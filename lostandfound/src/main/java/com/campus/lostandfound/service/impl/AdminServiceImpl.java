package com.campus.lostandfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostandfound.common.PageResult;
import com.campus.lostandfound.model.dto.ItemAdminSearchDTO;
import com.campus.lostandfound.model.dto.SendMessageDTO;
import com.campus.lostandfound.model.entity.Item;
import com.campus.lostandfound.model.entity.ItemImage;
import com.campus.lostandfound.model.entity.ItemTag;
import com.campus.lostandfound.model.entity.MatchRecord;
import com.campus.lostandfound.model.entity.User;
import com.campus.lostandfound.model.vo.DailyStatVO;
import com.campus.lostandfound.model.vo.ItemVO;
import com.campus.lostandfound.model.vo.StatisticsVO;
import com.campus.lostandfound.repository.ItemImageMapper;
import com.campus.lostandfound.repository.ItemMapper;
import com.campus.lostandfound.repository.ItemTagMapper;
import com.campus.lostandfound.repository.MatchRecordMapper;
import com.campus.lostandfound.repository.UserMapper;
import com.campus.lostandfound.service.AdminService;
import com.campus.lostandfound.service.MessageService;
import com.campus.lostandfound.exception.NotFoundException;
import com.campus.lostandfound.exception.BusinessException;
import com.campus.lostandfound.util.RedisUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 管理员服务实现类
 */
@Slf4j
@Service
public class AdminServiceImpl implements AdminService {
    
    private static final String STATISTICS_CACHE_KEY = "admin:statistics";
    private static final long CACHE_EXPIRE_MINUTES = 5;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ItemMapper itemMapper;
    
    @Autowired
    private MatchRecordMapper matchRecordMapper;
    
    @Autowired
    private ItemImageMapper itemImageMapper;
    
    @Autowired
    private ItemTagMapper itemTagMapper;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MessageService messageService;

    
    @Override
    public StatisticsVO getStatistics() {
        // 尝试从Redis缓存获取
        String cachedData = redisUtil.get(STATISTICS_CACHE_KEY);
        if (cachedData != null) {
            try {
                return objectMapper.readValue(cachedData, StatisticsVO.class);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse cached statistics data", e);
            }
        }
        
        // 缓存未命中，从数据库查询
        StatisticsVO statistics = buildStatisticsFromDatabase();
        
        // 存入Redis缓存，设置5分钟过期
        try {
            String jsonData = objectMapper.writeValueAsString(statistics);
            redisUtil.set(STATISTICS_CACHE_KEY, jsonData, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.warn("Failed to cache statistics data", e);
        }
        
        return statistics;
    }
    
    /**
     * 从数据库构建统计数据
     */
    private StatisticsVO buildStatisticsFromDatabase() {
        StatisticsVO statistics = new StatisticsVO();
        
        // 查询用户总数 (status=0 正常用户)
        Long totalUsers = userMapper.selectCount(
            new LambdaQueryWrapper<User>().eq(User::getStatus, 0)
        );
        statistics.setTotalUsers(totalUsers);
        
        // 查询信息总数 (deleted=0 未删除)
        // 注意：由于Item实体使用了@TableLogic，MyBatis-Plus会自动添加deleted=0条件
        Long totalItems = itemMapper.selectCount(new LambdaQueryWrapper<Item>());
        statistics.setTotalItems(totalItems);
        
        // 查询失物信息数 (type=0)
        Long totalLostItems = itemMapper.selectCount(
            new LambdaQueryWrapper<Item>().eq(Item::getType, 0)
        );
        statistics.setTotalLostItems(totalLostItems);
        
        // 查询招领信息数 (type=1)
        Long totalFoundItems = itemMapper.selectCount(
            new LambdaQueryWrapper<Item>().eq(Item::getType, 1)
        );
        statistics.setTotalFoundItems(totalFoundItems);
        
        // 查询匹配成功数 (status=1 已确认)
        Long totalMatched = matchRecordMapper.selectCount(
            new LambdaQueryWrapper<MatchRecord>().eq(MatchRecord::getStatus, 1)
        );
        statistics.setTotalMatched(totalMatched);
        
        // 计算匹配成功率: matchedCount * 2 / totalItems * 100%
        // 每次匹配成功涉及2个物品（1个失物+1个招领）
        if (totalItems > 0) {
            BigDecimal matchRate = BigDecimal.valueOf(totalMatched * 2)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalItems), 2, RoundingMode.HALF_UP);
            statistics.setMatchRate(matchRate);
        } else {
            statistics.setMatchRate(BigDecimal.ZERO);
        }
        
        // 查询今日新增用户
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Long todayNewUsers = userMapper.selectCount(
            new LambdaQueryWrapper<User>().ge(User::getCreatedAt, todayStart)
        );
        statistics.setTodayNewUsers(todayNewUsers.intValue());
        
        // 查询今日新增信息
        Long todayNewItems = itemMapper.selectCount(
            new LambdaQueryWrapper<Item>().ge(Item::getCreatedAt, todayStart)
        );
        statistics.setTodayNewItems(todayNewItems.intValue());
        
        // 查询近7天趋势
        List<DailyStatVO> weeklyTrend = buildWeeklyTrend();
        statistics.setWeeklyTrend(weeklyTrend);
        
        return statistics;
    }

    
    /**
     * 构建近7天数据趋势
     */
    private List<DailyStatVO> buildWeeklyTrend() {
        List<DailyStatVO> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
            
            DailyStatVO dailyStat = new DailyStatVO();
            dailyStat.setDate(date);
            
            // 当日新增用户数
            Long newUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                    .ge(User::getCreatedAt, dayStart)
                    .le(User::getCreatedAt, dayEnd)
            );
            dailyStat.setNewUsers(newUsers.intValue());
            
            // 当日新增信息数
            Long newItems = itemMapper.selectCount(
                new LambdaQueryWrapper<Item>()
                    .ge(Item::getCreatedAt, dayStart)
                    .le(Item::getCreatedAt, dayEnd)
            );
            dailyStat.setNewItems(newItems.intValue());
            
            // 当日匹配成功数 (使用confirmed_at字段)
            Long matchedCount = matchRecordMapper.selectCount(
                new LambdaQueryWrapper<MatchRecord>()
                    .eq(MatchRecord::getStatus, 1)
                    .ge(MatchRecord::getConfirmedAt, dayStart)
                    .le(MatchRecord::getConfirmedAt, dayEnd)
            );
            dailyStat.setMatchedCount(matchedCount.intValue());
            
            trend.add(dailyStat);
        }
        
        return trend;
    }
    
    @Override
    public PageResult<ItemVO> getItemList(ItemAdminSearchDTO dto) {
        // 构建查询条件，使用QueryWrapper以便绕过@TableLogic自动添加的deleted条件
        QueryWrapper<Item> queryWrapper = new QueryWrapper<>();
        
        // 关键词搜索（标题或描述）
        if (StringUtils.hasText(dto.getKeyword())) {
            queryWrapper.and(wrapper -> 
                wrapper.like("title", dto.getKeyword())
                    .or()
                    .like("description", dto.getKeyword())
            );
        }
        
        // 类型筛选
        if (dto.getType() != null) {
            queryWrapper.eq("type", dto.getType());
        }
        
        // 状态筛选
        if (dto.getStatus() != null) {
            queryWrapper.eq("status", dto.getStatus());
        }
        
        // 删除状态筛选（管理员可以查看已删除的物品）
        if (dto.getDeleted() != null) {
            queryWrapper.eq("deleted", dto.getDeleted());
        }
        // 如果deleted为null，则查询所有（包括已删除的）
        
        // 时间范围筛选
        if (dto.getStartTime() != null) {
            queryWrapper.ge("created_at", dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            queryWrapper.le("created_at", dto.getEndTime());
        }
        
        // 举报次数筛选（预留字段，当前数据库可能没有该字段）
        // 如果需要支持举报次数筛选，需要在item表中添加report_count字段
        // if (dto.getReportCount() != null) {
        //     queryWrapper.ge("report_count", dto.getReportCount());
        // }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc("created_at");
        
        // 分页查询
        Page<Item> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<Item> itemPage = itemMapper.selectPage(page, queryWrapper);
        
        // 转换为ItemVO列表
        List<ItemVO> itemVOList = convertToItemVOList(itemPage.getRecords());
        
        return new PageResult<>(
            itemVOList,
            itemPage.getTotal(),
            dto.getPageNum(),
            dto.getPageSize()
        );
    }
    
    /**
     * 将Item实体列表转换为ItemVO列表
     * 包含发布者信息、图片列表、标签列表
     */
    private List<ItemVO> convertToItemVOList(List<Item> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 收集所有物品ID和用户ID
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());
        List<Long> userIds = items.stream().map(Item::getUserId).distinct().collect(Collectors.toList());
        
        // 批量查询用户信息
        Map<Long, User> userMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        }
        
        // 批量查询图片信息
        Map<Long, List<String>> imageMap = Collections.emptyMap();
        if (!itemIds.isEmpty()) {
            List<ItemImage> images = itemImageMapper.selectList(
                new LambdaQueryWrapper<ItemImage>()
                    .in(ItemImage::getItemId, itemIds)
                    .orderByAsc(ItemImage::getSort)
            );
            imageMap = images.stream()
                .collect(Collectors.groupingBy(
                    ItemImage::getItemId,
                    Collectors.mapping(ItemImage::getUrl, Collectors.toList())
                ));
        }
        
        // 批量查询标签信息
        Map<Long, List<String>> tagMap = Collections.emptyMap();
        if (!itemIds.isEmpty()) {
            List<ItemTag> tags = itemTagMapper.selectList(
                new LambdaQueryWrapper<ItemTag>().in(ItemTag::getItemId, itemIds)
            );
            tagMap = tags.stream()
                .collect(Collectors.groupingBy(
                    ItemTag::getItemId,
                    Collectors.mapping(ItemTag::getTag, Collectors.toList())
                ));
        }
        
        // 转换为ItemVO
        final Map<Long, User> finalUserMap = userMap;
        final Map<Long, List<String>> finalImageMap = imageMap;
        final Map<Long, List<String>> finalTagMap = tagMap;
        
        return items.stream().map(item -> {
            ItemVO vo = new ItemVO();
            vo.setId(item.getId());
            vo.setUserId(item.getUserId());
            vo.setTitle(item.getTitle());
            vo.setDescription(item.getDescription());
            vo.setType(item.getType());
            vo.setCategory(item.getCategory());
            vo.setLongitude(item.getLongitude());
            vo.setLatitude(item.getLatitude());
            vo.setLocationDesc(item.getLocationDesc());
            vo.setEventTime(item.getEventTime());
            vo.setStatus(item.getStatus());
            vo.setViewCount(item.getViewCount());
            vo.setCreatedAt(item.getCreatedAt());
            vo.setUpdatedAt(item.getUpdatedAt());
            
            // 设置发布者信息
            User user = finalUserMap.get(item.getUserId());
            if (user != null) {
                vo.setUserName(user.getName());
                vo.setUserAvatar(user.getAvatar());
            }
            
            // 设置图片列表
            vo.setImages(finalImageMap.getOrDefault(item.getId(), Collections.emptyList()));
            
            // 设置标签列表
            vo.setTags(finalTagMap.getOrDefault(item.getId(), Collections.emptyList()));
            
            return vo;
        }).collect(Collectors.toList());
    }
    
    @Override
    public void reviewItem(Long itemId, Integer action, String reason) {
        // 验证action参数
        if (action == null || action < 0 || action > 2) {
            throw new BusinessException(400, "无效的审核操作类型");
        }
        
        // 使用QueryWrapper绕过@TableLogic，以便查询包括已删除的物品
        QueryWrapper<Item> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", itemId);
        Item item = itemMapper.selectOne(queryWrapper);
        
        if (item == null) {
            throw new NotFoundException("物品信息不存在");
        }
        
        switch (action) {
            case 0:
                // 通过: 清除举报标记（当前数据库没有report_count字段，预留逻辑）
                // 如果后续添加了report_count字段，可以在这里清零
                // item.setReportCount(0);
                // itemMapper.updateById(item);
                log.info("审核通过 - 物品ID: {}, 原因: {}", itemId, reason);
                break;
                
            case 1:
                // 删除: 设置deleted=1（软删除）
                // 由于@TableLogic注解，需要使用原生SQL或UpdateWrapper来更新deleted字段
                item.setDeleted(1);
                // 使用UpdateWrapper绕过@TableLogic
                QueryWrapper<Item> updateWrapper = new QueryWrapper<>();
                updateWrapper.eq("id", itemId);
                itemMapper.update(item, updateWrapper);
                log.info("审核删除 - 物品ID: {}, 原因: {}", itemId, reason);
                break;
                
            case 2:
                // 警告: 向发布者发送警告消息
                SendMessageDTO messageDTO = new SendMessageDTO();
                messageDTO.setUserId(item.getUserId());
                messageDTO.setTitle("内容审核警告");
                messageDTO.setContent(buildWarningMessage(item.getTitle(), reason));
                messageDTO.setType(0); // 系统通知
                messageDTO.setRelatedId(itemId);
                messageService.send(messageDTO);
                log.info("审核警告 - 物品ID: {}, 用户ID: {}, 原因: {}", itemId, item.getUserId(), reason);
                break;
                
            default:
                throw new BusinessException(400, "无效的审核操作类型");
        }
        
        // 记录审核日志
        log.info("管理员审核操作 - 物品ID: {}, 操作: {}, 原因: {}", itemId, getActionName(action), reason);
    }
    
    /**
     * 构建警告消息内容
     */
    private String buildWarningMessage(String itemTitle, String reason) {
        StringBuilder sb = new StringBuilder();
        sb.append("您发布的信息「").append(itemTitle).append("」已被管理员审核并发出警告。\n");
        if (reason != null && !reason.isEmpty()) {
            sb.append("警告原因: ").append(reason).append("\n");
        }
        sb.append("请遵守平台规则，发布合规内容。如有疑问，请联系管理员。");
        return sb.toString();
    }
    
    /**
     * 获取操作名称
     */
    private String getActionName(Integer action) {
        switch (action) {
            case 0: return "通过";
            case 1: return "删除";
            case 2: return "警告";
            default: return "未知";
        }
    }
    
    @Override
    public void banUser(Long userId, String reason) {
        // 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new NotFoundException("用户不存在");
        }
        
        // 检查用户是否已被封禁
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new BusinessException(400, "该用户已被封禁");
        }
        
        // 检查是否为管理员，管理员不能被封禁
        if (user.getRole() != null && user.getRole() == 1) {
            throw new BusinessException(400, "不能封禁管理员账户");
        }
        
        // 更新用户状态为封禁
        user.setStatus(1);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        // 向用户发送封禁通知消息
        SendMessageDTO messageDTO = new SendMessageDTO();
        messageDTO.setUserId(userId);
        messageDTO.setTitle("账户封禁通知");
        messageDTO.setContent(buildBanMessage(reason));
        messageDTO.setType(0); // 系统通知
        messageDTO.setRelatedId(userId);
        messageService.send(messageDTO);
        
        // 记录封禁日志
        log.info("用户封禁 - 用户ID: {}, 学号: {}, 姓名: {}, 原因: {}", 
                userId, user.getStudentId(), user.getName(), reason);
    }
    
    @Override
    public void unbanUser(Long userId) {
        // 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new NotFoundException("用户不存在");
        }
        
        // 检查用户是否处于封禁状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(400, "该用户未被封禁");
        }
        
        // 更新用户状态为正常
        user.setStatus(0);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        // 向用户发送解封通知消息
        SendMessageDTO messageDTO = new SendMessageDTO();
        messageDTO.setUserId(userId);
        messageDTO.setTitle("账户解封通知");
        messageDTO.setContent(buildUnbanMessage());
        messageDTO.setType(0); // 系统通知
        messageDTO.setRelatedId(userId);
        messageService.send(messageDTO);
        
        // 记录解封日志
        log.info("用户解封 - 用户ID: {}, 学号: {}, 姓名: {}", 
                userId, user.getStudentId(), user.getName());
    }
    
    /**
     * 构建封禁消息内容
     */
    private String buildBanMessage(String reason) {
        StringBuilder sb = new StringBuilder();
        sb.append("您的账户已被管理员封禁。\n");
        if (reason != null && !reason.isEmpty()) {
            sb.append("封禁原因: ").append(reason).append("\n");
        }
        sb.append("如有疑问，请联系管理员申诉。");
        return sb.toString();
    }
    
    /**
     * 构建解封消息内容
     */
    private String buildUnbanMessage() {
        return "您的账户已被解封，现在可以正常使用平台功能。\n请遵守平台规则，文明使用。";
    }
    
    @Override
    public byte[] exportReport(LocalDate startDate, LocalDate endDate) {
        // 验证日期参数
        if (startDate == null || endDate == null) {
            throw new BusinessException(400, "开始日期和结束日期不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(400, "开始日期不能晚于结束日期");
        }
        
        try (java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream()) {
            com.alibaba.excel.ExcelWriter excelWriter = com.alibaba.excel.EasyExcel.write(outputStream).build();
            
            // Sheet1: 概览数据
            List<com.campus.lostandfound.model.vo.export.OverviewExportVO> overviewData = buildOverviewData(startDate, endDate);
            com.alibaba.excel.write.metadata.WriteSheet sheet1 = com.alibaba.excel.EasyExcel.writerSheet(0, "概览数据")
                    .head(com.campus.lostandfound.model.vo.export.OverviewExportVO.class)
                    .build();
            excelWriter.write(overviewData, sheet1);
            
            // Sheet2: 每日明细数据
            List<com.campus.lostandfound.model.vo.export.DailyStatExportVO> dailyData = buildDailyStatData(startDate, endDate);
            com.alibaba.excel.write.metadata.WriteSheet sheet2 = com.alibaba.excel.EasyExcel.writerSheet(1, "每日明细")
                    .head(com.campus.lostandfound.model.vo.export.DailyStatExportVO.class)
                    .build();
            excelWriter.write(dailyData, sheet2);
            
            // Sheet3: 物品分类统计
            List<com.campus.lostandfound.model.vo.export.CategoryStatExportVO> categoryData = buildCategoryStatData(startDate, endDate);
            com.alibaba.excel.write.metadata.WriteSheet sheet3 = com.alibaba.excel.EasyExcel.writerSheet(2, "分类统计")
                    .head(com.campus.lostandfound.model.vo.export.CategoryStatExportVO.class)
                    .build();
            excelWriter.write(categoryData, sheet3);
            
            excelWriter.finish();
            
            return outputStream.toByteArray();
        } catch (java.io.IOException e) {
            log.error("导出Excel报表失败", e);
            throw new BusinessException(500, "导出报表失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建概览数据
     */
    private List<com.campus.lostandfound.model.vo.export.OverviewExportVO> buildOverviewData(LocalDate startDate, LocalDate endDate) {
        List<com.campus.lostandfound.model.vo.export.OverviewExportVO> data = new ArrayList<>();
        
        LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.MAX);
        
        // 时间范围内的用户总数
        Long totalUsers = userMapper.selectCount(
            new LambdaQueryWrapper<User>()
                .eq(User::getStatus, 0)
                .le(User::getCreatedAt, endDateTime)
        );
        data.add(new com.campus.lostandfound.model.vo.export.OverviewExportVO("用户总数", String.valueOf(totalUsers), "截至结束日期的正常用户数"));
        
        // 时间范围内新增用户
        Long newUsers = userMapper.selectCount(
            new LambdaQueryWrapper<User>()
                .ge(User::getCreatedAt, startDateTime)
                .le(User::getCreatedAt, endDateTime)
        );
        data.add(new com.campus.lostandfound.model.vo.export.OverviewExportVO("新增用户数", String.valueOf(newUsers), "时间范围内新注册用户数"));
        
        // 时间范围内的信息总数
        Long totalItems = itemMapper.selectCount(
            new LambdaQueryWrapper<Item>()
                .ge(Item::getCreatedAt, startDateTime)
                .le(Item::getCreatedAt, endDateTime)
        );
        data.add(new com.campus.lostandfound.model.vo.export.OverviewExportVO("信息总数", String.valueOf(totalItems), "时间范围内发布的信息数"));
        
        // 失物信息数
        Long lostItems = itemMapper.selectCount(
            new LambdaQueryWrapper<Item>()
                .eq(Item::getType, 0)
                .ge(Item::getCreatedAt, startDateTime)
                .le(Item::getCreatedAt, endDateTime)
        );
        data.add(new com.campus.lostandfound.model.vo.export.OverviewExportVO("失物信息数", String.valueOf(lostItems), "时间范围内发布的失物信息数"));
        
        // 招领信息数
        Long foundItems = itemMapper.selectCount(
            new LambdaQueryWrapper<Item>()
                .eq(Item::getType, 1)
                .ge(Item::getCreatedAt, startDateTime)
                .le(Item::getCreatedAt, endDateTime)
        );
        data.add(new com.campus.lostandfound.model.vo.export.OverviewExportVO("招领信息数", String.valueOf(foundItems), "时间范围内发布的招领信息数"));
        
        // 匹配成功数
        Long matchedCount = matchRecordMapper.selectCount(
            new LambdaQueryWrapper<MatchRecord>()
                .eq(MatchRecord::getStatus, 1)
                .ge(MatchRecord::getConfirmedAt, startDateTime)
                .le(MatchRecord::getConfirmedAt, endDateTime)
        );
        data.add(new com.campus.lostandfound.model.vo.export.OverviewExportVO("匹配成功数", String.valueOf(matchedCount), "时间范围内确认匹配成功的数量"));
        
        // 匹配成功率
        if (totalItems > 0) {
            BigDecimal matchRate = BigDecimal.valueOf(matchedCount * 2)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalItems), 2, RoundingMode.HALF_UP);
            data.add(new com.campus.lostandfound.model.vo.export.OverviewExportVO("匹配成功率", matchRate + "%", "匹配成功数*2/信息总数*100%"));
        } else {
            data.add(new com.campus.lostandfound.model.vo.export.OverviewExportVO("匹配成功率", "0%", "匹配成功数*2/信息总数*100%"));
        }
        
        // 统计时间范围
        data.add(new com.campus.lostandfound.model.vo.export.OverviewExportVO("统计开始日期", startDate.toString(), ""));
        data.add(new com.campus.lostandfound.model.vo.export.OverviewExportVO("统计结束日期", endDate.toString(), ""));
        
        return data;
    }
    
    /**
     * 构建每日明细数据
     */
    private List<com.campus.lostandfound.model.vo.export.DailyStatExportVO> buildDailyStatData(LocalDate startDate, LocalDate endDate) {
        List<com.campus.lostandfound.model.vo.export.DailyStatExportVO> data = new ArrayList<>();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            LocalDateTime dayStart = LocalDateTime.of(currentDate, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(currentDate, LocalTime.MAX);
            
            // 当日新增用户数
            Long newUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                    .ge(User::getCreatedAt, dayStart)
                    .le(User::getCreatedAt, dayEnd)
            );
            
            // 当日新增信息数
            Long newItems = itemMapper.selectCount(
                new LambdaQueryWrapper<Item>()
                    .ge(Item::getCreatedAt, dayStart)
                    .le(Item::getCreatedAt, dayEnd)
            );
            
            // 当日新增失物信息
            Long newLostItems = itemMapper.selectCount(
                new LambdaQueryWrapper<Item>()
                    .eq(Item::getType, 0)
                    .ge(Item::getCreatedAt, dayStart)
                    .le(Item::getCreatedAt, dayEnd)
            );
            
            // 当日新增招领信息
            Long newFoundItems = itemMapper.selectCount(
                new LambdaQueryWrapper<Item>()
                    .eq(Item::getType, 1)
                    .ge(Item::getCreatedAt, dayStart)
                    .le(Item::getCreatedAt, dayEnd)
            );
            
            // 当日匹配成功数
            Long matchedCount = matchRecordMapper.selectCount(
                new LambdaQueryWrapper<MatchRecord>()
                    .eq(MatchRecord::getStatus, 1)
                    .ge(MatchRecord::getConfirmedAt, dayStart)
                    .le(MatchRecord::getConfirmedAt, dayEnd)
            );
            
            data.add(new com.campus.lostandfound.model.vo.export.DailyStatExportVO(
                currentDate,
                newUsers.intValue(),
                newItems.intValue(),
                newLostItems.intValue(),
                newFoundItems.intValue(),
                matchedCount.intValue()
            ));
            
            currentDate = currentDate.plusDays(1);
        }
        
        return data;
    }
    
    /**
     * 构建物品分类统计数据
     */
    private List<com.campus.lostandfound.model.vo.export.CategoryStatExportVO> buildCategoryStatData(LocalDate startDate, LocalDate endDate) {
        List<com.campus.lostandfound.model.vo.export.CategoryStatExportVO> data = new ArrayList<>();
        
        LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.MAX);
        
        // 查询时间范围内所有物品的分类
        List<Item> items = itemMapper.selectList(
            new LambdaQueryWrapper<Item>()
                .ge(Item::getCreatedAt, startDateTime)
                .le(Item::getCreatedAt, endDateTime)
        );
        
        // 按分类分组统计
        Map<String, List<Item>> categoryMap = items.stream()
            .collect(Collectors.groupingBy(item -> 
                item.getCategory() != null ? item.getCategory() : "未分类"
            ));
        
        // 查询时间范围内的匹配记录
        List<MatchRecord> matchRecords = matchRecordMapper.selectList(
            new LambdaQueryWrapper<MatchRecord>()
                .eq(MatchRecord::getStatus, 1)
                .ge(MatchRecord::getConfirmedAt, startDateTime)
                .le(MatchRecord::getConfirmedAt, endDateTime)
        );
        
        // 获取匹配成功的物品ID集合
        java.util.Set<Long> matchedItemIds = new java.util.HashSet<>();
        for (MatchRecord record : matchRecords) {
            matchedItemIds.add(record.getLostItemId());
            matchedItemIds.add(record.getFoundItemId());
        }
        
        for (Map.Entry<String, List<Item>> entry : categoryMap.entrySet()) {
            String category = entry.getKey();
            List<Item> categoryItems = entry.getValue();
            
            long lostCount = categoryItems.stream().filter(i -> i.getType() == 0).count();
            long foundCount = categoryItems.stream().filter(i -> i.getType() == 1).count();
            long totalCount = categoryItems.size();
            
            // 计算该分类下匹配成功的物品数
            long matchedCount = categoryItems.stream()
                .filter(i -> matchedItemIds.contains(i.getId()))
                .count();
            
            // 计算匹配率
            BigDecimal matchRate = BigDecimal.ZERO;
            if (totalCount > 0) {
                matchRate = BigDecimal.valueOf(matchedCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
            }
            
            data.add(new com.campus.lostandfound.model.vo.export.CategoryStatExportVO(
                category,
                lostCount,
                foundCount,
                totalCount,
                matchedCount,
                matchRate
            ));
        }
        
        // 按总数量降序排序
        data.sort((a, b) -> Long.compare(b.getTotalCount(), a.getTotalCount()));
        
        return data;
    }
}
