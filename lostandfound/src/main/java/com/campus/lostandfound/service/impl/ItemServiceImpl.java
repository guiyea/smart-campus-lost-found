package com.campus.lostandfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostandfound.common.PageResult;
import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.exception.NotFoundException;
import com.campus.lostandfound.model.dto.ItemDTO;
import com.campus.lostandfound.model.dto.ItemSearchDTO;
import com.campus.lostandfound.model.entity.Item;
import com.campus.lostandfound.model.entity.ItemImage;
import com.campus.lostandfound.model.entity.ItemTag;
import com.campus.lostandfound.model.entity.User;
import com.campus.lostandfound.model.vo.ItemDetailVO;
import com.campus.lostandfound.model.vo.ItemVO;
import com.campus.lostandfound.model.vo.MatchVO;
import com.campus.lostandfound.repository.ItemImageMapper;
import com.campus.lostandfound.repository.ItemMapper;
import com.campus.lostandfound.repository.ItemTagMapper;
import com.campus.lostandfound.repository.UserMapper;
import com.campus.lostandfound.service.ImageRecognitionService;
import com.campus.lostandfound.service.ItemService;
import com.campus.lostandfound.service.LocationService;
import com.campus.lostandfound.service.MatchService;
import com.campus.lostandfound.service.PointService;
import com.campus.lostandfound.model.vo.GeoPoint;
import com.campus.lostandfound.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 物品信息服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    
    private final ItemMapper itemMapper;
    private final ItemImageMapper itemImageMapper;
    private final ItemTagMapper itemTagMapper;
    private final UserMapper userMapper;
    private final PointService pointService;
    private final ImageRecognitionService imageRecognitionService;
    private final MatchService matchService;
    private final LocationService locationService;
    private final RedisUtil redisUtil;
    
    /**
     * 发布失物/招领信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ItemVO publish(ItemDTO dto, Long userId) {
        log.info("用户 {} 发布物品信息: {}", userId, dto.getTitle());
        
        // 1. 创建Item实体
        Item item = new Item();
        BeanUtils.copyProperties(dto, item);
        item.setUserId(userId);
        item.setStatus(0);  // 待处理
        item.setViewCount(0);
        item.setDeleted(0);
        
        // 2. 保存Item到数据库
        itemMapper.insert(item);
        log.info("物品信息保存成功，ID: {}", item.getId());
        
        // 3. 批量保存图片到item_image表
        List<ItemImage> images = new ArrayList<>();
        for (int i = 0; i < dto.getImages().size(); i++) {
            ItemImage image = new ItemImage();
            image.setItemId(item.getId());
            image.setUrl(dto.getImages().get(i));
            image.setSort(i);
            images.add(image);
        }
        images.forEach(itemImageMapper::insert);
        log.info("保存 {} 张图片", images.size());
        
        // 4. 如果type=1(招领)，调用PointService为用户增加10积分
        if (dto.getType() == 1) {
            pointService.addPoints(userId, 10, "发布招领信息", item.getId());
            log.info("用户 {} 发布招领信息，增加10积分", userId);
        }
        
        // 5. 异步调用ImageRecognitionService识别图片，保存标签到item_tag表
        if (!dto.getImages().isEmpty()) {
            asyncRecognizeImage(dto.getImages().get(0), item.getId());
        }
        
        // 6. 异步调用MatchService执行匹配计算
        asyncCalculateMatch(item);
        
        // 7. 返回ItemVO
        return convertToVO(item);
    }
    
    /**
     * 更新物品信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ItemVO update(Long id, ItemDTO dto, Long userId) {
        log.info("用户 {} 更新物品信息: itemId={}", userId, id);
        
        // 1. 查询物品信息，不存在则抛出NotFoundException
        Item item = itemMapper.selectById(id);
        if (item == null || item.getDeleted() == 1) {
            log.warn("物品不存在或已删除: itemId={}", id);
            throw new NotFoundException("物品信息不存在");
        }
        
        // 2. 验证userId是否为物品发布者，不是则抛出ForbiddenException
        if (!item.getUserId().equals(userId)) {
            log.warn("用户 {} 无权修改物品 {}", userId, id);
            throw new com.campus.lostandfound.exception.ForbiddenException("无权修改他人发布的信息");
        }
        
        // 3. 查询旧图片列表，用于判断图片是否变更
        LambdaQueryWrapper<ItemImage> oldImageWrapper = new LambdaQueryWrapper<>();
        oldImageWrapper.eq(ItemImage::getItemId, id);
        List<ItemImage> oldImages = itemImageMapper.selectList(oldImageWrapper);
        List<String> oldImageUrls = oldImages.stream()
                                             .map(ItemImage::getUrl)
                                             .collect(Collectors.toList());
        
        // 4. 更新物品基本信息
        BeanUtils.copyProperties(dto, item);
        item.setId(id);  // 确保ID不变
        item.setUserId(userId);  // 确保发布者不变
        itemMapper.updateById(item);
        log.info("物品基本信息更新成功: itemId={}", id);
        
        // 5. 删除旧图片记录，插入新图片记录
        LambdaQueryWrapper<ItemImage> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ItemImage::getItemId, id);
        itemImageMapper.delete(deleteWrapper);
        
        List<ItemImage> newImages = new ArrayList<>();
        for (int i = 0; i < dto.getImages().size(); i++) {
            ItemImage image = new ItemImage();
            image.setItemId(id);
            image.setUrl(dto.getImages().get(i));
            image.setSort(i);
            newImages.add(image);
        }
        newImages.forEach(itemImageMapper::insert);
        log.info("更新图片记录: 删除 {} 张，新增 {} 张", oldImages.size(), newImages.size());
        
        // 6. 如果图片变更，重新调用AI识别更新标签
        boolean imagesChanged = !oldImageUrls.equals(dto.getImages());
        if (imagesChanged && !dto.getImages().isEmpty()) {
            log.info("图片已变更，重新识别: itemId={}", id);
            // 删除旧标签
            LambdaQueryWrapper<ItemTag> deleteTagWrapper = new LambdaQueryWrapper<>();
            deleteTagWrapper.eq(ItemTag::getItemId, id);
            itemTagMapper.delete(deleteTagWrapper);
            
            // 异步识别新图片
            asyncRecognizeImage(dto.getImages().get(0), id);
        }
        
        // 7. 返回更新后的ItemVO
        return convertToVO(item);
    }
    
    /**
     * 删除物品信息（软删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        log.info("用户 {} 删除物品信息: itemId={}", userId, id);
        
        // 1. 查询物品信息，不存在则抛出NotFoundException
        Item item = itemMapper.selectById(id);
        if (item == null || item.getDeleted() == 1) {
            log.warn("物品不存在或已删除: itemId={}", id);
            throw new NotFoundException("物品信息不存在");
        }
        
        // 2. 验证userId是否为物品发布者，不是则抛出ForbiddenException
        if (!item.getUserId().equals(userId)) {
            log.warn("用户 {} 无权删除物品 {}", userId, id);
            throw new com.campus.lostandfound.exception.ForbiddenException("无权删除他人发布的信息");
        }
        
        // 3. 设置deleted=1（软删除），不物理删除数据，保留原始记录
        item.setDeleted(1);
        itemMapper.updateById(item);
        log.info("物品信息软删除成功: itemId={}", id);
    }
    
    /**
     * 获取物品详情
     */
    @Override
    public ItemDetailVO getDetail(Long id) {
        log.info("获取物品详情: itemId={}", id);
        
        // 1. 查询物品信息（包含关联的图片和标签），不存在或已删除则抛出NotFoundException
        Item item = itemMapper.selectById(id);
        if (item == null || item.getDeleted() == 1) {
            log.warn("物品不存在或已删除: itemId={}", id);
            throw new NotFoundException("物品信息不存在");
        }
        
        // 2. 使用Redis原子操作增加浏览次数: INCR item:view:{id}
        String viewKey = "item:view:" + id;
        Long currentViewCount = redisUtil.increment(viewKey);
        log.info("物品 {} 浏览次数增加到: {}", id, currentViewCount);
        
        // 3. 定时任务批量同步浏览次数到数据库（或直接更新）
        // 这里选择直接更新数据库，也可以通过定时任务批量同步
        item.setViewCount(currentViewCount.intValue());
        itemMapper.updateById(item);
        
        // 4. 查询发布者信息
        User user = userMapper.selectById(item.getUserId());
        
        // 5. 调用MatchService获取匹配推荐列表（前10条）
        Result<List<MatchVO>> matchResult = matchService.getRecommendations(id);
        List<MatchVO> matchRecommendations = matchResult.getData() != null ? matchResult.getData() : new ArrayList<>();
        
        // 6. 组装ItemDetailVO返回
        ItemDetailVO detailVO = new ItemDetailVO();
        BeanUtils.copyProperties(item, detailVO);
        
        // 设置发布者信息
        if (user != null) {
            detailVO.setUserName(user.getName());
            detailVO.setUserAvatar(user.getAvatar());
        }
        
        // 查询图片列表
        LambdaQueryWrapper<ItemImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(ItemImage::getItemId, id)
                   .orderByAsc(ItemImage::getSort);
        List<ItemImage> images = itemImageMapper.selectList(imageWrapper);
        detailVO.setImages(images.stream()
                                .map(ItemImage::getUrl)
                                .collect(Collectors.toList()));
        
        // 查询标签列表
        LambdaQueryWrapper<ItemTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(ItemTag::getItemId, id);
        List<ItemTag> tags = itemTagMapper.selectList(tagWrapper);
        detailVO.setTags(tags.stream()
                            .map(ItemTag::getTag)
                            .collect(Collectors.toList()));
        
        // 设置匹配推荐列表
        detailVO.setMatchRecommendations(matchRecommendations);
        
        log.info("物品详情查询成功: itemId={}, 推荐数量={}", id, matchRecommendations.size());
        return detailVO;
    }
    
    /**
     * 异步识别图片
     */
    @Async
    protected void asyncRecognizeImage(String imageUrl, Long itemId) {
        try {
            log.info("开始异步识别图片: itemId={}", itemId);
            imageRecognitionService.recognizeAndSaveTags(imageUrl, itemId);
        } catch (Exception e) {
            log.error("图片识别失败: itemId={}", itemId, e);
        }
    }
    
    /**
     * 异步执行匹配计算
     */
    @Async
    protected void asyncCalculateMatch(Item item) {
        try {
            log.info("开始异步匹配计算: itemId={}", item.getId());
            matchService.calculateMatchAsync(item);
        } catch (Exception e) {
            log.error("匹配计算失败: itemId={}", item.getId(), e);
        }
    }
    
    /**
     * 转换为ItemVO
     */
    @Override
    public ItemVO convertToVO(Item item) {
        ItemVO vo = new ItemVO();
        BeanUtils.copyProperties(item, vo);
        
        // 查询发布者信息
        User user = userMapper.selectById(item.getUserId());
        if (user != null) {
            vo.setUserName(user.getName());
            vo.setUserAvatar(user.getAvatar());
        }
        
        // 查询图片列表
        LambdaQueryWrapper<ItemImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(ItemImage::getItemId, item.getId())
                   .orderByAsc(ItemImage::getSort);
        List<ItemImage> images = itemImageMapper.selectList(imageWrapper);
        vo.setImages(images.stream()
                          .map(ItemImage::getUrl)
                          .collect(Collectors.toList()));
        
        // 查询标签列表
        LambdaQueryWrapper<ItemTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(ItemTag::getItemId, item.getId());
        List<ItemTag> tags = itemTagMapper.selectList(tagWrapper);
        vo.setTags(tags.stream()
                      .map(ItemTag::getTag)
                      .collect(Collectors.toList()));
        
        return vo;
    }
    
    /**
     * 手动更新物品类别
     * 用于AI识别失败或识别不准确时的降级方案
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ItemVO updateCategory(Long id, String category, Long userId) {
        log.info("用户 {} 手动更新物品类别: itemId={}, category={}", userId, id, category);
        
        // 1. 查询物品信息，不存在则抛出NotFoundException
        Item item = itemMapper.selectById(id);
        if (item == null || item.getDeleted() == 1) {
            log.warn("物品不存在或已删除: itemId={}", id);
            throw new NotFoundException("物品信息不存在");
        }
        
        // 2. 验证userId是否为物品发布者，不是则抛出ForbiddenException
        if (!item.getUserId().equals(userId)) {
            log.warn("用户 {} 无权修改物品 {} 的类别", userId, id);
            throw new com.campus.lostandfound.exception.ForbiddenException("无权修改他人发布的信息");
        }
        
        // 3. 更新类别
        item.setCategory(category);
        itemMapper.updateById(item);
        log.info("物品类别更新成功: itemId={}, category={}", id, category);
        
        // 4. 返回更新后的ItemVO
        return convertToVO(item);
    }
    
    /**
     * 获取附近的物品信息
     * 基于用户当前位置返回指定半径范围内的物品列表
     */
    @Override
    public List<ItemVO> getNearby(Double lng, Double lat, Integer radius) {
        log.info("查询附近物品: 中心点=({}, {}), 半径={}米", lng, lat, radius);
        
        // 1. 参数校验和默认值处理
        if (lng == null || lat == null) {
            log.warn("查询附近物品失败：坐标为空");
            return new ArrayList<>();
        }
        
        // 默认半径1000米
        if (radius == null || radius <= 0) {
            radius = 1000;
        }
        
        // 2. 调用LocationService.searchInRadius()获取范围内物品ID
        List<Long> itemIds = locationService.searchInRadius(lng, lat, radius);
        
        if (itemIds.isEmpty()) {
            log.info("附近无物品: 中心点=({}, {}), 半径={}米", lng, lat, radius);
            return new ArrayList<>();
        }
        
        log.info("范围内找到 {} 个物品ID", itemIds.size());
        
        // 3. 批量查询物品信息
        List<Item> items = itemMapper.selectBatchIds(itemIds);
        
        if (items.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 4. 转换为ItemVO并计算距离
        GeoPoint center = GeoPoint.of(lng, lat);
        
        List<ItemVO> result = items.stream()
                .map(item -> {
                    ItemVO vo = convertToVO(item);
                    
                    // 计算每个物品与中心点的距离
                    if (item.getLongitude() != null && item.getLatitude() != null) {
                        GeoPoint itemPoint = GeoPoint.of(
                                item.getLongitude().doubleValue(),
                                item.getLatitude().doubleValue()
                        );
                        Double distance = locationService.calculateDistance(center, itemPoint);
                        vo.setDistance(distance);
                    }
                    
                    return vo;
                })
                // 5. 按距离升序排序
                .sorted(Comparator.comparing(ItemVO::getDistance, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        
        log.info("附近物品查询完成: 中心点=({}, {}), 半径={}米, 结果数={}", lng, lat, radius, result.size());
        
        return result;
    }
    
    /**
     * 搜索物品列表
     * 支持关键词搜索、多条件筛选、地理范围筛选和多种排序方式
     */
    @Override
    public PageResult<ItemVO> search(ItemSearchDTO dto) {
        log.info("搜索物品: keyword={}, type={}, category={}, status={}, sortBy={}, pageNum={}, pageSize={}",
                dto.getKeyword(), dto.getType(), dto.getCategory(), dto.getStatus(), 
                dto.getSortBy(), dto.getPageNum(), dto.getPageSize());
        
        // 1. 参数校验和默认值处理
        int pageNum = dto.getPageNum() != null && dto.getPageNum() > 0 ? dto.getPageNum() : 1;
        int pageSize = dto.getPageSize() != null && dto.getPageSize() > 0 ? dto.getPageSize() : 20;
        // 限制每页最大20条
        if (pageSize > 20) {
            pageSize = 20;
        }
        
        // 2. 如果有地理范围筛选，先获取范围内的物品ID
        Set<Long> geoFilteredIds = null;
        if (dto.getLongitude() != null && dto.getLatitude() != null && dto.getRadius() != null && dto.getRadius() > 0) {
            List<Long> nearbyIds = locationService.searchInRadius(
                    dto.getLongitude().doubleValue(),
                    dto.getLatitude().doubleValue(),
                    dto.getRadius()
            );
            geoFilteredIds = nearbyIds.stream().collect(Collectors.toSet());
            log.info("地理范围筛选: 中心点=({}, {}), 半径={}米, 匹配数={}",
                    dto.getLongitude(), dto.getLatitude(), dto.getRadius(), geoFilteredIds.size());
            
            // 如果地理范围内没有物品，直接返回空结果
            if (geoFilteredIds.isEmpty()) {
                return new PageResult<>(new ArrayList<>(), 0L, pageNum, pageSize);
            }
        }
        
        // 3. 如果有关键词，先查询匹配的标签对应的物品ID
        Set<Long> tagMatchedIds = null;
        if (StringUtils.hasText(dto.getKeyword())) {
            LambdaQueryWrapper<ItemTag> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.like(ItemTag::getTag, dto.getKeyword());
            List<ItemTag> matchedTags = itemTagMapper.selectList(tagWrapper);
            tagMatchedIds = matchedTags.stream()
                    .map(ItemTag::getItemId)
                    .collect(Collectors.toSet());
            log.info("标签关键词匹配: keyword={}, 匹配物品数={}", dto.getKeyword(), tagMatchedIds.size());
        }
        
        // 4. 构建MyBatis-Plus QueryWrapper
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        
        // deleted = 0（排除已删除）- 由于Item实体使用了@TableLogic，MyBatis-Plus会自动处理
        // 但为了明确，我们显式添加条件
        queryWrapper.eq(Item::getDeleted, 0);
        
        // 如果keyword不为空: title LIKE %keyword% OR description LIKE %keyword% OR 存在匹配的tag
        if (StringUtils.hasText(dto.getKeyword())) {
            final Set<Long> finalTagMatchedIds = tagMatchedIds;
            queryWrapper.and(wrapper -> {
                wrapper.like(Item::getTitle, dto.getKeyword())
                       .or()
                       .like(Item::getDescription, dto.getKeyword());
                // 如果有标签匹配的物品ID，添加到OR条件中
                if (finalTagMatchedIds != null && !finalTagMatchedIds.isEmpty()) {
                    wrapper.or().in(Item::getId, finalTagMatchedIds);
                }
            });
        }
        
        // 如果type不为空: type = dto.type
        if (dto.getType() != null) {
            queryWrapper.eq(Item::getType, dto.getType());
        }
        
        // 如果category不为空: category = dto.category
        if (StringUtils.hasText(dto.getCategory())) {
            queryWrapper.eq(Item::getCategory, dto.getCategory());
        }
        
        // 如果status不为空: status = dto.status
        if (dto.getStatus() != null) {
            queryWrapper.eq(Item::getStatus, dto.getStatus());
        }
        
        // 如果startTime不为空: event_time >= startTime
        if (dto.getStartTime() != null) {
            queryWrapper.ge(Item::getEventTime, dto.getStartTime());
        }
        
        // 如果endTime不为空: event_time <= endTime
        if (dto.getEndTime() != null) {
            queryWrapper.le(Item::getEventTime, dto.getEndTime());
        }
        
        // 如果有地理范围: 使用子查询筛选范围内的物品ID
        if (geoFilteredIds != null) {
            queryWrapper.in(Item::getId, geoFilteredIds);
        }
        
        // 5. 根据sortBy设置排序
        String sortBy = dto.getSortBy();
        boolean needDistanceSort = "distance".equalsIgnoreCase(sortBy) && 
                dto.getLongitude() != null && dto.getLatitude() != null;
        
        if (!needDistanceSort) {
            // time: ORDER BY created_at DESC (默认排序)
            // match: 预留，后续实现匹配度排序，目前使用时间排序
            queryWrapper.orderByDesc(Item::getCreatedAt);
        }
        
        // 6. 使用MyBatis-Plus分页插件进行分页
        Page<Item> page = new Page<>(pageNum, pageSize);
        Page<Item> resultPage = itemMapper.selectPage(page, queryWrapper);
        
        log.info("查询结果: 总数={}, 当前页数量={}", resultPage.getTotal(), resultPage.getRecords().size());
        
        // 7. 转换为ItemVO列表
        List<ItemVO> voList = resultPage.getRecords().stream()
                .map(item -> {
                    ItemVO vo = convertToVO(item);
                    
                    // 如果需要按距离排序，计算距离
                    if (dto.getLongitude() != null && dto.getLatitude() != null &&
                            item.getLongitude() != null && item.getLatitude() != null) {
                        GeoPoint center = GeoPoint.of(dto.getLongitude().doubleValue(), dto.getLatitude().doubleValue());
                        GeoPoint itemPoint = GeoPoint.of(item.getLongitude().doubleValue(), item.getLatitude().doubleValue());
                        Double distance = locationService.calculateDistance(center, itemPoint);
                        vo.setDistance(distance);
                    }
                    
                    return vo;
                })
                .collect(Collectors.toList());
        
        // 8. 如果需要按距离排序，在内存中排序
        if (needDistanceSort) {
            voList = voList.stream()
                    .sorted(Comparator.comparing(ItemVO::getDistance, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
        }
        
        // 9. 返回PageResult<ItemVO>
        return new PageResult<>(voList, resultPage.getTotal(), pageNum, pageSize);
    }
    
    /**
     * 获取热门物品列表
     * 返回最近7天浏览量最高的物品
     */
    @Override
    public List<ItemVO> getHotItems(Integer limit) {
        log.info("获取热门物品: limit={}", limit);
        
        // 1. 参数校验和默认值处理
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        // 限制最大返回数量为20
        if (limit > 20) {
            limit = 20;
        }
        
        // 2. 计算7天前的时间
        java.time.LocalDateTime sevenDaysAgo = java.time.LocalDateTime.now().minusDays(7);
        
        // 3. 构建查询条件：最近7天、未删除、按浏览量降序
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getDeleted, 0)
                   .ge(Item::getCreatedAt, sevenDaysAgo)
                   .orderByDesc(Item::getViewCount)
                   .last("LIMIT " + limit);
        
        // 4. 查询物品列表
        List<Item> items = itemMapper.selectList(queryWrapper);
        
        log.info("热门物品查询完成: 结果数={}", items.size());
        
        // 5. 转换为ItemVO列表
        List<ItemVO> result = items.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return result;
    }
}
