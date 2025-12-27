package com.campus.lostandfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.exception.NotFoundException;
import com.campus.lostandfound.exception.ForbiddenException;
import com.campus.lostandfound.model.dto.MatchFeedbackDTO;
import com.campus.lostandfound.model.entity.Item;
import com.campus.lostandfound.model.entity.ItemImage;
import com.campus.lostandfound.model.entity.ItemTag;
import com.campus.lostandfound.model.entity.MatchRecord;
import com.campus.lostandfound.model.entity.Message;
import com.campus.lostandfound.model.entity.User;
import com.campus.lostandfound.model.entity.MatchFeedback;
import com.campus.lostandfound.model.vo.GeoPoint;
import com.campus.lostandfound.model.vo.MatchResult;
import com.campus.lostandfound.model.vo.MatchVO;
import com.campus.lostandfound.repository.ItemImageMapper;
import com.campus.lostandfound.repository.ItemMapper;
import com.campus.lostandfound.repository.ItemTagMapper;
import com.campus.lostandfound.repository.MatchRecordMapper;
import com.campus.lostandfound.repository.MessageMapper;
import com.campus.lostandfound.repository.UserMapper;
import com.campus.lostandfound.repository.MatchFeedbackMapper;
import com.campus.lostandfound.service.LocationService;
import com.campus.lostandfound.service.MatchService;
import com.campus.lostandfound.service.PointService;
import com.campus.lostandfound.model.vo.ItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 匹配服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {
    
    private final ItemMapper itemMapper;
    private final ItemTagMapper itemTagMapper;
    private final MatchRecordMapper matchRecordMapper;
    private final MessageMapper messageMapper;
    private final LocationService locationService;
    // Removed ItemService dependency to avoid circular reference
    private final PointService pointService;
    private final UserMapper userMapper;
    private final ItemImageMapper itemImageMapper;
    private final MatchFeedbackMapper matchFeedbackMapper;
    
    @Override
    public List<MatchResult> calculateMatch(Item item) {
        log.info("开始计算匹配 - itemId: {}, type: {}", item.getId(), item.getType());
        
        // 查询相反类型的物品列表（失物找招领，招领找失物）
        Integer targetType = item.getType() == 0 ? 1 : 0;
        
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getType, targetType)
                   .eq(Item::getStatus, 0)  // 待处理
                   .eq(Item::getDeleted, 0) // 未删除
                   .ne(Item::getUserId, item.getUserId()); // 排除自己发布的
        
        List<Item> candidateItems = itemMapper.selectList(queryWrapper);
        log.info("找到候选物品数量: {}", candidateItems.size());
        
        if (candidateItems.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取当前物品的标签
        List<String> itemTags = getItemTags(item.getId());
        
        List<MatchResult> matchResults = new ArrayList<>();
        
        for (Item candidate : candidateItems) {
            MatchResult matchResult = new MatchResult(item.getId(), candidate.getId());
            
            // 计算各项分数
            BigDecimal categoryScore = calculateCategoryScore(item.getCategory(), candidate.getCategory());
            BigDecimal tagScore = calculateTagScore(itemTags, getItemTags(candidate.getId()));
            BigDecimal timeScore = calculateTimeScore(item.getEventTime(), candidate.getEventTime());
            BigDecimal locationScore = calculateLocationScore(item, candidate);
            
            matchResult.setCategoryScore(categoryScore);
            matchResult.setTagScore(tagScore);
            matchResult.setTimeScore(timeScore);
            matchResult.setLocationScore(locationScore);
            
            // 计算总分
            BigDecimal totalScore = categoryScore.add(tagScore).add(timeScore).add(locationScore);
            matchResult.setScore(totalScore);
            
            matchResults.add(matchResult);
        }
        
        // 按分数降序排序
        matchResults.sort((a, b) -> b.getScore().compareTo(a.getScore()));
        
        log.info("匹配计算完成，共计算 {} 个候选物品", matchResults.size());
        return matchResults;
    }
    
    @Override
    public Result<List<MatchVO>> getRecommendations(Long itemId) {
        log.info("获取匹配推荐 - itemId: {}", itemId);
        
        // 查询物品信息
        Item item = itemMapper.selectById(itemId);
        if (item == null || item.getDeleted() == 1) {
            throw new NotFoundException("物品不存在");
        }
        
        // 计算匹配
        List<MatchResult> matchResults = calculateMatch(item);
        
        // 取分数最高的前10条
        List<MatchResult> topMatches = matchResults.stream()
                .limit(10)
                .collect(Collectors.toList());
        
        // 查询对应的物品详情并组装MatchVO
        List<MatchVO> matchVOs = new ArrayList<>();
        for (MatchResult matchResult : topMatches) {
            try {
                Item matchedItem = itemMapper.selectById(matchResult.getMatchedItemId());
                if (matchedItem != null && matchedItem.getDeleted() == 0) {
                    MatchVO matchVO = new MatchVO();
                    // 复制ItemVO的基本属性
                    BeanUtils.copyProperties(convertToVO(matchedItem), matchVO);
                    
                    // 设置匹配分数
                    matchVO.setMatchScore(matchResult.getScore());
                    matchVO.setCategoryScore(matchResult.getCategoryScore());
                    matchVO.setTagScore(matchResult.getTagScore());
                    matchVO.setTimeScore(matchResult.getTimeScore());
                    matchVO.setLocationScore(matchResult.getLocationScore());
                    
                    matchVOs.add(matchVO);
                }
            } catch (Exception e) {
                log.error("获取匹配物品详情失败 - itemId: {}", matchResult.getMatchedItemId(), e);
            }
        }
        
        log.info("返回匹配推荐数量: {}", matchVOs.size());
        return Result.success(matchVOs);
    }
    
    @Override
    @Transactional
    public Result<Void> confirmMatch(Long itemId, Long matchedItemId, Long userId) {
        log.info("确认匹配 - itemId: {}, matchedItemId: {}, userId: {}", itemId, matchedItemId, userId);
        
        // 验证两个物品信息
        Item item1 = itemMapper.selectById(itemId);
        Item item2 = itemMapper.selectById(matchedItemId);
        
        if (item1 == null || item1.getDeleted() == 1) {
            throw new NotFoundException("物品不存在");
        }
        if (item2 == null || item2.getDeleted() == 1) {
            throw new NotFoundException("匹配物品不存在");
        }
        
        // 验证用户是其中一个物品的发布者
        if (!userId.equals(item1.getUserId()) && !userId.equals(item2.getUserId())) {
            throw new ForbiddenException("只有物品发布者才能确认匹配");
        }
        
        // 验证物品状态为待处理
        if (item1.getStatus() != 0 || item2.getStatus() != 0) {
            throw new ForbiddenException("只有待处理状态的物品才能确认匹配");
        }
        
        // 确定失物和招领物品
        Item lostItem = item1.getType() == 0 ? item1 : item2;
        Item foundItem = item1.getType() == 1 ? item1 : item2;
        
        // 计算匹配分数
        List<MatchResult> matchResults = calculateMatch(lostItem);
        BigDecimal matchScore = matchResults.stream()
                .filter(result -> result.getMatchedItemId().equals(foundItem.getId()))
                .findFirst()
                .map(MatchResult::getScore)
                .orElse(BigDecimal.ZERO);
        
        // 创建匹配记录
        MatchRecord matchRecord = new MatchRecord();
        matchRecord.setLostItemId(lostItem.getId());
        matchRecord.setFoundItemId(foundItem.getId());
        matchRecord.setScore(matchScore);
        matchRecord.setStatus(1); // 已确认
        matchRecord.setCreatedAt(LocalDateTime.now());
        matchRecord.setConfirmedAt(LocalDateTime.now());
        matchRecordMapper.insert(matchRecord);
        
        // 更新两个物品的状态为已找回
        lostItem.setStatus(1);
        foundItem.setStatus(1);
        itemMapper.updateById(lostItem);
        itemMapper.updateById(foundItem);
        
        // 为招领信息的发布者增加50积分
        try {
            pointService.addPoints(foundItem.getUserId(), 50, "HELP_FIND", foundItem.getId());
        } catch (Exception e) {
            log.error("增加积分失败", e);
        }
        
        // 向双方发送匹配成功的站内消息
        try {
            sendMatchSuccessMessage(lostItem, foundItem);
        } catch (Exception e) {
            log.error("发送匹配成功消息失败", e);
        }
        
        log.info("匹配确认成功 - lostItemId: {}, foundItemId: {}", lostItem.getId(), foundItem.getId());
        return Result.success();
    }
    
    @Override
    @Transactional
    public Result<Void> feedback(MatchFeedbackDTO dto) {
        log.info("收到匹配反馈 - itemId: {}, matchedItemId: {}, userId: {}, isAccurate: {}", 
                dto.getItemId(), dto.getMatchedItemId(), dto.getUserId(), dto.getIsAccurate());
        
        // 验证物品存在
        Item item = itemMapper.selectById(dto.getItemId());
        Item matchedItem = itemMapper.selectById(dto.getMatchedItemId());
        
        if (item == null || item.getDeleted() == 1) {
            throw new NotFoundException("物品不存在");
        }
        if (matchedItem == null || matchedItem.getDeleted() == 1) {
            throw new NotFoundException("匹配物品不存在");
        }
        
        // 验证用户ID
        if (dto.getUserId() == null) {
            throw new ForbiddenException("用户ID不能为空");
        }
        
        // 验证用户是否有权限反馈（必须是其中一个物品的发布者）
        if (!dto.getUserId().equals(item.getUserId()) && !dto.getUserId().equals(matchedItem.getUserId())) {
            throw new ForbiddenException("只有物品发布者才能提供匹配反馈");
        }
        
        // 计算当前匹配分数（用于记录）
        List<MatchResult> matchResults = calculateMatch(item);
        BigDecimal currentMatchScore = matchResults.stream()
                .filter(result -> result.getMatchedItemId().equals(matchedItem.getId()))
                .findFirst()
                .map(MatchResult::getScore)
                .orElse(BigDecimal.ZERO);
        
        // 创建反馈记录
        MatchFeedback feedback = new MatchFeedback();
        feedback.setItemId(dto.getItemId());
        feedback.setMatchedItemId(dto.getMatchedItemId());
        feedback.setUserId(dto.getUserId());
        feedback.setIsAccurate(dto.getIsAccurate());
        feedback.setComment(dto.getComment());
        feedback.setMatchScore(currentMatchScore);
        feedback.setItemCategory(item.getCategory());
        feedback.setMatchedItemCategory(matchedItem.getCategory());
        feedback.setCreatedAt(LocalDateTime.now());
        
        // 保存反馈记录到数据库
        matchFeedbackMapper.insert(feedback);
        
        // 记录反馈数据用于后续算法优化
        log.info("匹配反馈已保存 - 反馈ID: {}, 物品类别: {} -> {}, 准确性: {}, 匹配分数: {}", 
                feedback.getId(), item.getCategory(), matchedItem.getCategory(), 
                dto.getIsAccurate(), currentMatchScore);
        
        // 如果反馈不准确，记录用于后续算法优化
        if (!dto.getIsAccurate()) {
            log.warn("收到不准确匹配反馈 - 物品类别: {} -> {}, 匹配分数: {}, 评论: {}", 
                    item.getCategory(), matchedItem.getCategory(), currentMatchScore, dto.getComment());
            
            // TODO: 可以在这里实现算法权重调整逻辑
            // 例如：降低相同类别组合的匹配权重
            // 或者：记录到算法优化队列中，定期批量处理
        }
        
        log.info("匹配反馈处理完成 - itemId: {}, matchedItemId: {}", dto.getItemId(), dto.getMatchedItemId());
        return Result.success();
    }
    
    @Override
    @Async
    public void calculateMatchAsync(Item item) {
        log.info("异步执行匹配计算 - itemId: {}", item.getId());

        try {
            List<MatchResult> matchResults = calculateMatch(item);

            // 筛选匹配分数>=70的结果
            List<MatchResult> highScoreMatches = matchResults.stream()
                    .filter(result -> result.getScore().compareTo(new BigDecimal("70")) >= 0)
                    .collect(Collectors.toList());

            if (!highScoreMatches.isEmpty()) {
                log.info("发现高分匹配 {} 个，发送通知", highScoreMatches.size());
                sendMatchNotifications(item, highScoreMatches);
            }
        } catch (Exception e) {
            log.error("异步匹配计算失败 - itemId: {}", item.getId(), e);
        }
    }

    @Override
    public Result<List<MatchVO>> getUserMatchRecommendations(Long userId) {
        log.info("获取用户匹配推荐 - userId: {}", userId);

        // 查询用户所有待处理的物品
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getUserId, userId)
                   .eq(Item::getStatus, 0)  // 待处理
                   .eq(Item::getDeleted, 0); // 未删除

        List<Item> userItems = itemMapper.selectList(queryWrapper);
        log.info("用户待处理物品数量: {}", userItems.size());

        if (userItems.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // 收集所有物品的匹配结果
        List<MatchVO> allMatches = new ArrayList<>();

        for (Item item : userItems) {
            try {
                // 计算该物品的匹配
                List<MatchResult> matchResults = calculateMatch(item);

                // 取前3个最佳匹配
                List<MatchResult> topMatches = matchResults.stream()
                        .limit(3)
                        .collect(Collectors.toList());

                // 转换为MatchVO
                for (MatchResult matchResult : topMatches) {
                    Item matchedItem = itemMapper.selectById(matchResult.getMatchedItemId());
                    if (matchedItem != null && matchedItem.getDeleted() == 0) {
                        MatchVO matchVO = new MatchVO();
                        BeanUtils.copyProperties(convertToVO(matchedItem), matchVO);
                        matchVO.setMatchScore(matchResult.getScore());
                        matchVO.setCategoryScore(matchResult.getCategoryScore());
                        matchVO.setTagScore(matchResult.getTagScore());
                        matchVO.setTimeScore(matchResult.getTimeScore());
                        matchVO.setLocationScore(matchResult.getLocationScore());
                        allMatches.add(matchVO);
                    }
                }
            } catch (Exception e) {
                log.error("获取物品匹配失败 - itemId: {}", item.getId(), e);
            }
        }

        // 按匹配分数降序排序，取前10条
        List<MatchVO> topRecommendations = allMatches.stream()
                .sorted((a, b) -> b.getMatchScore().compareTo(a.getMatchScore()))
                .limit(10)
                .collect(Collectors.toList());

        log.info("返回用户匹配推荐数量: {}", topRecommendations.size());
        return Result.success(topRecommendations);
    }

    /**
     * 获取物品标签列表
     */
    private List<String> getItemTags(Long itemId) {
        LambdaQueryWrapper<ItemTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ItemTag::getItemId, itemId);
        
        List<ItemTag> itemTags = itemTagMapper.selectList(queryWrapper);
        return itemTags.stream()
                .map(ItemTag::getTag)
                .collect(Collectors.toList());
    }
    
    /**
     * 计算类别匹配分数
     */
    private BigDecimal calculateCategoryScore(String category1, String category2) {
        if (category1 != null && category1.equals(category2)) {
            return new BigDecimal("30");
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * 计算标签匹配分数（使用Jaccard相似度）
     */
    private BigDecimal calculateTagScore(List<String> tags1, List<String> tags2) {
        if (tags1.isEmpty() && tags2.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        Set<String> set1 = new HashSet<>(tags1);
        Set<String> set2 = new HashSet<>(tags2);
        
        // 计算交集
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        // 计算并集
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        if (union.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Jaccard相似度 = |交集| / |并集|
        double jaccard = (double) intersection.size() / union.size();
        
        // 乘以30得到标签分数
        return new BigDecimal(jaccard * 30).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算时间匹配分数
     */
    private BigDecimal calculateTimeScore(LocalDateTime time1, LocalDateTime time2) {
        if (time1 == null || time2 == null) {
            return BigDecimal.ZERO;
        }
        
        long daysDiff = Math.abs(ChronoUnit.DAYS.between(time1, time2));
        
        if (daysDiff > 7) {
            return BigDecimal.ZERO;
        }
        
        // score = max(0, 20 - (daysDiff * 20 / 7))
        double score = Math.max(0, 20 - (daysDiff * 20.0 / 7));
        return new BigDecimal(score).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算位置匹配分数
     */
    private BigDecimal calculateLocationScore(Item item1, Item item2) {
        if (item1.getLongitude() == null || item1.getLatitude() == null ||
            item2.getLongitude() == null || item2.getLatitude() == null) {
            return BigDecimal.ZERO;
        }
        
        try {
            GeoPoint point1 = new GeoPoint();
            point1.setLongitude(item1.getLongitude().doubleValue());
            point1.setLatitude(item1.getLatitude().doubleValue());
            
            GeoPoint point2 = new GeoPoint();
            point2.setLongitude(item2.getLongitude().doubleValue());
            point2.setLatitude(item2.getLatitude().doubleValue());
            
            Double distance = locationService.calculateDistance(point1, point2);
            if (distance == null) {
                return BigDecimal.ZERO;
            }
            
            double distanceKm = distance / 1000.0;
            
            if (distanceKm > 5) {
                return BigDecimal.ZERO;
            }
            
            // score = max(0, 20 - (distanceKm * 20 / 5))
            double score = Math.max(0, 20 - (distanceKm * 20.0 / 5));
            return new BigDecimal(score).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("计算位置分数失败", e);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 发送匹配通知
     */
    private void sendMatchNotifications(Item item, List<MatchResult> highScoreMatches) {
        try {
            for (MatchResult matchResult : highScoreMatches) {
                Item matchedItem = itemMapper.selectById(matchResult.getMatchedItemId());
                if (matchedItem != null) {
                    // 构建匹配通知消息内容，包含匹配物品的简要信息和链接
                    String itemTypeText = item.getType() == 0 ? "失物" : "招领";
                    String matchedTypeText = matchedItem.getType() == 0 ? "失物" : "招领";
                    
                    // 向物品发布者发送通知
                    String messageToItemOwner = String.format(
                        "发现可能匹配的%s：%s，匹配度：%.1f%%。地点：%s，时间：%s。点击查看详情：/items/%d", 
                        matchedTypeText,
                        matchedItem.getTitle(), 
                        matchResult.getScore().doubleValue(),
                        matchedItem.getLocationDesc() != null ? matchedItem.getLocationDesc() : "未知",
                        matchedItem.getEventTime() != null ? matchedItem.getEventTime().toString() : "未知",
                        matchedItem.getId()
                    );
                    
                    // 向匹配物品发布者发送通知
                    String messageToMatchedOwner = String.format(
                        "发现可能匹配的%s：%s，匹配度：%.1f%%。地点：%s，时间：%s。点击查看详情：/items/%d", 
                        itemTypeText,
                        item.getTitle(), 
                        matchResult.getScore().doubleValue(),
                        item.getLocationDesc() != null ? item.getLocationDesc() : "未知",
                        item.getEventTime() != null ? item.getEventTime().toString() : "未知",
                        item.getId()
                    );
                    
                    // 创建并保存消息记录
                    Message messageForItemOwner = new Message();
                    messageForItemOwner.setUserId(item.getUserId());
                    messageForItemOwner.setTitle("发现匹配物品");
                    messageForItemOwner.setContent(messageToItemOwner);
                    messageForItemOwner.setType(1); // 匹配通知
                    messageForItemOwner.setRelatedId(matchedItem.getId());
                    messageForItemOwner.setIsRead(0); // 未读
                    messageForItemOwner.setCreatedAt(LocalDateTime.now());
                    messageMapper.insert(messageForItemOwner);
                    
                    Message messageForMatchedOwner = new Message();
                    messageForMatchedOwner.setUserId(matchedItem.getUserId());
                    messageForMatchedOwner.setTitle("发现匹配物品");
                    messageForMatchedOwner.setContent(messageToMatchedOwner);
                    messageForMatchedOwner.setType(1); // 匹配通知
                    messageForMatchedOwner.setRelatedId(item.getId());
                    messageForMatchedOwner.setIsRead(0); // 未读
                    messageForMatchedOwner.setCreatedAt(LocalDateTime.now());
                    messageMapper.insert(messageForMatchedOwner);
                    
                    log.info("发送匹配通知成功 - 物品ID: {}, 匹配物品ID: {}, 匹配度: {}", 
                            item.getId(), matchedItem.getId(), matchResult.getScore());
                }
            }
        } catch (Exception e) {
            log.error("发送匹配通知失败", e);
        }
    }
    
    /**
     * 发送匹配成功消息
     */
    private void sendMatchSuccessMessage(Item lostItem, Item foundItem) {
        try {
            String lostMessage = String.format("恭喜！您的失物\"%s\"已找到，感谢用户的帮助！", lostItem.getTitle());
            String foundMessage = String.format("恭喜！您帮助找回了失物\"%s\"，获得50积分奖励！", lostItem.getTitle());
            
            // 向失物发布者发送消息
            Message messageForLostOwner = new Message();
            messageForLostOwner.setUserId(lostItem.getUserId());
            messageForLostOwner.setTitle("物品找回成功");
            messageForLostOwner.setContent(lostMessage);
            messageForLostOwner.setType(1); // 匹配通知
            messageForLostOwner.setRelatedId(foundItem.getId());
            messageForLostOwner.setIsRead(0); // 未读
            messageForLostOwner.setCreatedAt(LocalDateTime.now());
            messageMapper.insert(messageForLostOwner);
            
            // 向招领发布者发送消息
            Message messageForFoundOwner = new Message();
            messageForFoundOwner.setUserId(foundItem.getUserId());
            messageForFoundOwner.setTitle("帮助找回物品");
            messageForFoundOwner.setContent(foundMessage);
            messageForFoundOwner.setType(1); // 匹配通知
            messageForFoundOwner.setRelatedId(lostItem.getId());
            messageForFoundOwner.setIsRead(0); // 未读
            messageForFoundOwner.setCreatedAt(LocalDateTime.now());
            messageMapper.insert(messageForFoundOwner);
            
            log.info("发送匹配成功消息完成 - 失物ID: {}, 招领ID: {}", lostItem.getId(), foundItem.getId());
        } catch (Exception e) {
            log.error("发送匹配成功消息失败", e);
        }
    }
    
    /**
     * 将Item实体转换为ItemVO
     * 避免循环依赖，直接在此处实现转换逻辑
     */
    private ItemVO convertToVO(Item item) {
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
}
