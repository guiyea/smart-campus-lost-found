package com.campus.lostandfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostandfound.common.PageResult;
import com.campus.lostandfound.model.entity.PointRecord;
import com.campus.lostandfound.model.entity.User;
import com.campus.lostandfound.model.enums.PointReason;
import com.campus.lostandfound.model.vo.PointRankVO;
import com.campus.lostandfound.model.vo.PointRecordVO;
import com.campus.lostandfound.repository.PointRecordMapper;
import com.campus.lostandfound.repository.UserMapper;
import com.campus.lostandfound.service.CacheService;
import com.campus.lostandfound.service.PointService;
import com.campus.lostandfound.util.RedisUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 积分服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {
    
    // 缓存键
    private static final String POINT_RANKING_KEY = "point:ranking";
    
    // 缓存过期时间（10分钟）
    private static final long RANKING_CACHE_MINUTES = 10;
    
    private final PointRecordMapper pointRecordMapper;
    private final UserMapper userMapper;
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;
    private final CacheService cacheService;
    
    /**
     * 增加用户积分
     * 使用原子操作更新用户积分，确保并发安全
     * 同时使用CacheService更新Redis ZSET排行榜缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Long userId, Integer points, String reason, Long relatedId) {
        log.info("为用户 {} 增加 {} 积分，原因: {}", userId, points, reason);
        
        // 创建积分记录
        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setPoints(points);
        record.setReason(reason);
        record.setRelatedId(relatedId);
        pointRecordMapper.insert(record);
        
        // 使用原子操作更新用户总积分（避免并发问题）
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId)
                .setSql("points = points + " + points);
        int updated = userMapper.update(null, updateWrapper);
        
        if (updated > 0) {
            User user = userMapper.selectById(userId);
            log.info("用户 {} 当前总积分: {}", userId, user != null ? user.getPoints() : "未知");
            
            // 使用CacheService更新Redis ZSET排行榜缓存
            cacheService.updatePointRankingScore(userId, points);
        } else {
            log.warn("用户 {} 积分更新失败，用户可能不存在", userId);
        }
    }
    
    /**
     * 获取用户积分明细
     */
    @Override
    public PageResult<PointRecordVO> getRecords(Long userId, Integer pageNum, Integer pageSize) {
        log.info("查询用户 {} 的积分明细，页码: {}, 每页: {}", userId, pageNum, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<PointRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PointRecord::getUserId, userId)
                .orderByDesc(PointRecord::getCreatedAt);
        
        // 分页查询
        Page<PointRecord> page = new Page<>(pageNum, pageSize);
        Page<PointRecord> resultPage = pointRecordMapper.selectPage(page, queryWrapper);
        
        // 转换为VO
        List<PointRecordVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(voList, resultPage.getTotal(), pageNum, pageSize);
    }
    
    /**
     * 获取积分排行榜
     * 使用Redis ZSET缓存，过期时间10分钟
     */
    @Override
    public List<PointRankVO> getRanking(Integer limit) {
        // 限制最大返回数量为100
        int actualLimit = Math.min(limit != null ? limit : 100, 100);
        log.info("查询积分排行榜，限制数量: {}", actualLimit);
        
        // 1. 尝试从Redis ZSET缓存获取
        if (Boolean.TRUE.equals(redisUtil.hasKey(POINT_RANKING_KEY))) {
            Set<ZSetOperations.TypedTuple<String>> cachedRanking = 
                    redisUtil.zRevRangeWithScores(POINT_RANKING_KEY, 0, actualLimit - 1);
            
            if (cachedRanking != null && !cachedRanking.isEmpty()) {
                log.info("积分排行榜缓存命中，数量: {}", cachedRanking.size());
                return buildRankingFromCache(cachedRanking);
            }
        }
        
        // 2. 缓存未命中，从数据库查询
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getStatus, 0)  // 只查询正常状态的用户
                .orderByDesc(User::getPoints)
                .last("LIMIT " + actualLimit);
        
        List<User> users = userMapper.selectList(queryWrapper);
        
        // 3. 构建排行榜并存入Redis ZSET缓存
        Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();
        List<PointRankVO> rankList = new ArrayList<>();
        
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            
            // 构建VO
            PointRankVO rankVO = new PointRankVO();
            rankVO.setRank(i + 1);
            rankVO.setUserId(user.getId());
            rankVO.setUserName(user.getName());
            rankVO.setUserAvatar(user.getAvatar());
            rankVO.setPoints(user.getPoints() != null ? user.getPoints() : 0);
            rankList.add(rankVO);
            
            // 构建ZSET元素（存储用户ID和积分）
            final int points = user.getPoints() != null ? user.getPoints() : 0;
            tuples.add(new ZSetOperations.TypedTuple<String>() {
                @Override
                public String getValue() {
                    return String.valueOf(user.getId());
                }
                
                @Override
                public Double getScore() {
                    return (double) points;
                }
                
                @Override
                public int compareTo(ZSetOperations.TypedTuple<String> o) {
                    return Double.compare(o.getScore(), this.getScore());
                }
            });
        }
        
        // 4. 存入Redis ZSET缓存
        if (!tuples.isEmpty()) {
            redisUtil.zAdd(POINT_RANKING_KEY, tuples);
            redisUtil.expire(POINT_RANKING_KEY, RANKING_CACHE_MINUTES, TimeUnit.MINUTES);
            log.info("积分排行榜已缓存到Redis ZSET，数量: {}", tuples.size());
        }
        
        return rankList;
    }
    
    /**
     * 从缓存构建排行榜
     */
    private List<PointRankVO> buildRankingFromCache(Set<ZSetOperations.TypedTuple<String>> cachedRanking) {
        List<PointRankVO> rankList = new ArrayList<>();
        int rank = 1;
        
        // 收集所有用户ID
        List<Long> userIds = cachedRanking.stream()
                .map(tuple -> Long.parseLong(tuple.getValue()))
                .collect(Collectors.toList());
        
        // 批量查询用户信息
        List<User> users = userMapper.selectBatchIds(userIds);
        java.util.Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        
        for (ZSetOperations.TypedTuple<String> tuple : cachedRanking) {
            Long userId = Long.parseLong(tuple.getValue());
            User user = userMap.get(userId);
            
            if (user != null) {
                PointRankVO rankVO = new PointRankVO();
                rankVO.setRank(rank++);
                rankVO.setUserId(userId);
                rankVO.setUserName(user.getName());
                rankVO.setUserAvatar(user.getAvatar());
                rankVO.setPoints(tuple.getScore() != null ? tuple.getScore().intValue() : 0);
                rankList.add(rankVO);
            }
        }
        
        return rankList;
    }
    
    /**
     * 获取用户总积分
     */
    @Override
    public Integer getTotalPoints(Long userId) {
        log.info("查询用户 {} 的总积分", userId);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户 {} 不存在", userId);
            return 0;
        }
        
        return user.getPoints() != null ? user.getPoints() : 0;
    }
    
    /**
     * 将PointRecord实体转换为PointRecordVO
     */
    private PointRecordVO convertToVO(PointRecord record) {
        PointRecordVO vo = new PointRecordVO();
        vo.setId(record.getId());
        vo.setPoints(record.getPoints());
        vo.setReason(record.getReason());
        vo.setReasonDesc(PointReason.getDescriptionByCode(record.getReason()));
        vo.setRelatedId(record.getRelatedId());
        vo.setCreatedAt(record.getCreatedAt());
        return vo;
    }
}
