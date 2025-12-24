package com.campus.lostandfound.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campus.lostandfound.model.entity.PointRecord;
import com.campus.lostandfound.model.entity.User;
import com.campus.lostandfound.model.enums.PointReason;
import com.campus.lostandfound.model.vo.PointRankVO;
import com.campus.lostandfound.repository.PointRecordMapper;
import com.campus.lostandfound.repository.UserMapper;
import com.campus.lostandfound.service.impl.PointServiceImpl;
import com.campus.lostandfound.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 积分奖励属性测试
 * 
 * Feature: smart-campus-lost-found, Property 14: 积分奖励正确性
 * Validates: Requirements 7.1, 7.2, 7.3, 7.4
 */
class PointServicePropertyTest {

    /**
     * Property 14: 积分奖励正确性 - 发布招领信息应增加10积分
     * 
     * Feature: smart-campus-lost-found, Property 14: 积分奖励正确性
     * 测试: 对于任意用户发布招领信息，积分应增加10
     * Validates: Requirements 7.2
     */
    @Property(tries = 100)
    void publishFoundItemShouldAdd10Points(
            @ForAll("validUserIds") Long userId,
            @ForAll("validRelatedIds") Long relatedId) {
        
        // Setup mocks
        PointRecordMapper pointRecordMapper = mock(PointRecordMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        ObjectMapper objectMapper = new ObjectMapper();
        CacheService cacheService = mock(CacheService.class);
        
        PointServiceImpl pointService = new PointServiceImpl(pointRecordMapper, userMapper, redisUtil, objectMapper, cacheService);
        
        // Mock user exists with initial points
        User user = new User();
        user.setId(userId);
        user.setPoints(0);
        when(userMapper.selectById(userId)).thenReturn(user);
        when(userMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(pointRecordMapper.insert(any(PointRecord.class))).thenReturn(1);
        
        // Execute: add points for publishing found item
        int expectedPoints = PointReason.PUBLISH_FOUND.getPoints(); // Should be 10
        pointService.addPoints(userId, expectedPoints, PointReason.PUBLISH_FOUND.getCode(), relatedId);
        
        // Verify: PointRecord was created with correct points
        ArgumentCaptor<PointRecord> recordCaptor = ArgumentCaptor.forClass(PointRecord.class);
        verify(pointRecordMapper).insert(recordCaptor.capture());
        
        PointRecord capturedRecord = recordCaptor.getValue();
        assertEquals(10, capturedRecord.getPoints(),
            "Publishing found item should add exactly 10 points");
        assertEquals(userId, capturedRecord.getUserId(),
            "PointRecord should have correct userId");
        assertEquals(PointReason.PUBLISH_FOUND.getCode(), capturedRecord.getReason(),
            "PointRecord should have PUBLISH_FOUND reason");
        assertEquals(relatedId, capturedRecord.getRelatedId(),
            "PointRecord should have correct relatedId");
    }

    /**
     * Property 14: 积分奖励正确性 - 帮助找回物品应增加50积分
     * 
     * Feature: smart-campus-lost-found, Property 14: 积分奖励正确性
     * 测试: 对于任意用户帮助找回物品，积分应增加50
     * Validates: Requirements 7.1
     */
    @Property(tries = 100)
    void helpFindItemShouldAdd50Points(
            @ForAll("validUserIds") Long userId,
            @ForAll("validRelatedIds") Long relatedId) {
        
        // Setup mocks
        PointRecordMapper pointRecordMapper = mock(PointRecordMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        ObjectMapper objectMapper = new ObjectMapper();
        CacheService cacheService = mock(CacheService.class);
        
        PointServiceImpl pointService = new PointServiceImpl(pointRecordMapper, userMapper, redisUtil, objectMapper, cacheService);
        
        // Mock user exists
        User user = new User();
        user.setId(userId);
        user.setPoints(0);
        when(userMapper.selectById(userId)).thenReturn(user);
        when(userMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(pointRecordMapper.insert(any(PointRecord.class))).thenReturn(1);
        
        // Execute: add points for helping find item
        int expectedPoints = PointReason.HELP_FIND.getPoints(); // Should be 50
        pointService.addPoints(userId, expectedPoints, PointReason.HELP_FIND.getCode(), relatedId);
        
        // Verify: PointRecord was created with correct points
        ArgumentCaptor<PointRecord> recordCaptor = ArgumentCaptor.forClass(PointRecord.class);
        verify(pointRecordMapper).insert(recordCaptor.capture());
        
        PointRecord capturedRecord = recordCaptor.getValue();
        assertEquals(50, capturedRecord.getPoints(),
            "Helping find item should add exactly 50 points");
        assertEquals(userId, capturedRecord.getUserId(),
            "PointRecord should have correct userId");
        assertEquals(PointReason.HELP_FIND.getCode(), capturedRecord.getReason(),
            "PointRecord should have HELP_FIND reason");
    }

    /**
     * Property 14: 积分奖励正确性 - 每日首次登录应增加2积分
     * 
     * Feature: smart-campus-lost-found, Property 14: 积分奖励正确性
     * 测试: 对于任意用户每日首次登录，积分应增加2
     * Validates: Requirements 7.3
     */
    @Property(tries = 100)
    void dailyLoginShouldAdd2Points(
            @ForAll("validUserIds") Long userId) {
        
        // Setup mocks
        PointRecordMapper pointRecordMapper = mock(PointRecordMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        ObjectMapper objectMapper = new ObjectMapper();
        CacheService cacheService = mock(CacheService.class);
        
        PointServiceImpl pointService = new PointServiceImpl(pointRecordMapper, userMapper, redisUtil, objectMapper, cacheService);
        
        // Mock user exists
        User user = new User();
        user.setId(userId);
        user.setPoints(0);
        when(userMapper.selectById(userId)).thenReturn(user);
        when(userMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(pointRecordMapper.insert(any(PointRecord.class))).thenReturn(1);
        
        // Execute: add points for daily login
        int expectedPoints = PointReason.DAILY_LOGIN.getPoints(); // Should be 2
        pointService.addPoints(userId, expectedPoints, PointReason.DAILY_LOGIN.getCode(), null);
        
        // Verify: PointRecord was created with correct points
        ArgumentCaptor<PointRecord> recordCaptor = ArgumentCaptor.forClass(PointRecord.class);
        verify(pointRecordMapper).insert(recordCaptor.capture());
        
        PointRecord capturedRecord = recordCaptor.getValue();
        assertEquals(2, capturedRecord.getPoints(),
            "Daily login should add exactly 2 points");
        assertEquals(userId, capturedRecord.getUserId(),
            "PointRecord should have correct userId");
        assertEquals(PointReason.DAILY_LOGIN.getCode(), capturedRecord.getReason(),
            "PointRecord should have DAILY_LOGIN reason");
    }

    /**
     * Property 14: 积分奖励正确性 - 任意积分变动应创建对应的PointRecord记录
     * 
     * Feature: smart-campus-lost-found, Property 14: 积分奖励正确性
     * 测试: 对于任意积分变动，应创建对应的PointRecord记录
     * Validates: Requirements 7.4
     */
    @Property(tries = 100)
    void anyPointChangeShouldCreatePointRecord(
            @ForAll("validUserIds") Long userId,
            @ForAll("validPoints") Integer points,
            @ForAll("validReasons") String reason,
            @ForAll("validRelatedIds") Long relatedId) {
        
        // Setup mocks
        PointRecordMapper pointRecordMapper = mock(PointRecordMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        ObjectMapper objectMapper = new ObjectMapper();
        CacheService cacheService = mock(CacheService.class);
        
        PointServiceImpl pointService = new PointServiceImpl(pointRecordMapper, userMapper, redisUtil, objectMapper, cacheService);
        
        // Mock user exists
        User user = new User();
        user.setId(userId);
        user.setPoints(0);
        when(userMapper.selectById(userId)).thenReturn(user);
        when(userMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(pointRecordMapper.insert(any(PointRecord.class))).thenReturn(1);
        
        // Execute: add points
        pointService.addPoints(userId, points, reason, relatedId);
        
        // Verify: PointRecord was created
        ArgumentCaptor<PointRecord> recordCaptor = ArgumentCaptor.forClass(PointRecord.class);
        verify(pointRecordMapper, times(1)).insert(recordCaptor.capture());
        
        PointRecord capturedRecord = recordCaptor.getValue();
        assertNotNull(capturedRecord, "PointRecord should be created for any point change");
        assertEquals(userId, capturedRecord.getUserId(),
            "PointRecord should have correct userId");
        assertEquals(points, capturedRecord.getPoints(),
            "PointRecord should have correct points value");
        assertEquals(reason, capturedRecord.getReason(),
            "PointRecord should have correct reason");
        assertEquals(relatedId, capturedRecord.getRelatedId(),
            "PointRecord should have correct relatedId");
    }

    /**
     * Property 14: 积分奖励正确性 - 验证PointReason枚举值正确性
     * 
     * Feature: smart-campus-lost-found, Property 14: 积分奖励正确性
     * 测试: 验证积分原因枚举的积分值符合需求规范
     * Validates: Requirements 7.1, 7.2, 7.3
     */
    @Property(tries = 100)
    void pointReasonEnumShouldHaveCorrectValues(
            @ForAll("pointReasons") PointReason reason) {
        
        switch (reason) {
            case DAILY_LOGIN:
                assertEquals(2, reason.getPoints(),
                    "DAILY_LOGIN should award 2 points");
                break;
            case PUBLISH_FOUND:
                assertEquals(10, reason.getPoints(),
                    "PUBLISH_FOUND should award 10 points");
                break;
            case HELP_FIND:
                assertEquals(50, reason.getPoints(),
                    "HELP_FIND should award 50 points");
                break;
            default:
                fail("Unknown PointReason: " + reason);
        }
    }

    // ==================== Generators ====================

    /**
     * Generator for valid user IDs (positive Long values)
     */
    @Provide
    Arbitrary<Long> validUserIds() {
        return Arbitraries.longs().between(1L, 1000000L);
    }

    /**
     * Generator for valid related IDs (positive Long values or null)
     */
    @Provide
    Arbitrary<Long> validRelatedIds() {
        return Arbitraries.longs().between(1L, 1000000L);
    }

    /**
     * Generator for valid points (positive integers)
     */
    @Provide
    Arbitrary<Integer> validPoints() {
        return Arbitraries.integers().between(1, 100);
    }

    /**
     * Generator for valid reasons (from PointReason enum codes)
     */
    @Provide
    Arbitrary<String> validReasons() {
        return Arbitraries.of(
            PointReason.DAILY_LOGIN.getCode(),
            PointReason.PUBLISH_FOUND.getCode(),
            PointReason.HELP_FIND.getCode()
        );
    }

    /**
     * Generator for PointReason enum values
     */
    @Provide
    Arbitrary<PointReason> pointReasons() {
        return Arbitraries.of(PointReason.values());
    }

    // ==================== Property 15: 积分排行榜排序正确性 ====================

    /**
     * Property 15: 积分排行榜排序正确性 - 结果应按points降序排列
     * 
     * Feature: smart-campus-lost-found, Property 15: 积分排行榜排序正确性
     * 测试: 对于任意排行榜查询，结果应按points降序排列
     * Validates: Requirements 7.5
     */
    @Property(tries = 100)
    void rankingShouldBeOrderedByPointsDescending(
            @ForAll("validLimits") Integer limit) {
        
        // Setup mocks
        PointRecordMapper pointRecordMapper = mock(PointRecordMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        ObjectMapper objectMapper = new ObjectMapper();
        CacheService cacheService = mock(CacheService.class);
        
        PointServiceImpl pointService = new PointServiceImpl(pointRecordMapper, userMapper, redisUtil, objectMapper, cacheService);
        
        // Create mock users with random points (already sorted by points desc to simulate DB behavior)
        List<User> mockUsers = createMockUsersForRanking(limit);
        
        when(userMapper.selectList(any())).thenReturn(mockUsers);
        
        // Execute: get ranking
        List<PointRankVO> ranking = pointService.getRanking(limit);
        
        // Verify: results are ordered by points descending
        for (int i = 0; i < ranking.size() - 1; i++) {
            PointRankVO current = ranking.get(i);
            PointRankVO next = ranking.get(i + 1);
            assertTrue(current.getPoints() >= next.getPoints(),
                String.format("Ranking should be ordered by points descending. " +
                    "Position %d has %d points, position %d has %d points",
                    i, current.getPoints(), i + 1, next.getPoints()));
        }
    }

    /**
     * Property 15: 积分排行榜排序正确性 - 结果数量应<=100
     * 
     * Feature: smart-campus-lost-found, Property 15: 积分排行榜排序正确性
     * 测试: 对于任意排行榜查询(limit=100)，结果数量应<=100
     * Validates: Requirements 7.5
     */
    @Property(tries = 100)
    void rankingShouldNotExceed100Results(
            @ForAll("limitsUpTo200") Integer requestedLimit) {
        
        // Setup mocks
        PointRecordMapper pointRecordMapper = mock(PointRecordMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        ObjectMapper objectMapper = new ObjectMapper();
        CacheService cacheService = mock(CacheService.class);
        
        PointServiceImpl pointService = new PointServiceImpl(pointRecordMapper, userMapper, redisUtil, objectMapper, cacheService);
        
        // Create mock users - simulate DB returning up to the actual limit
        int actualLimit = Math.min(requestedLimit != null ? requestedLimit : 100, 100);
        List<User> mockUsers = createMockUsersForRanking(actualLimit);
        
        when(userMapper.selectList(any())).thenReturn(mockUsers);
        
        // Execute: get ranking
        List<PointRankVO> ranking = pointService.getRanking(requestedLimit);
        
        // Verify: result count should not exceed 100
        assertTrue(ranking.size() <= 100,
            String.format("Ranking should not exceed 100 results, but got %d", ranking.size()));
    }

    /**
     * Property 15: 积分排行榜排序正确性 - 相邻排名的积分关系
     * 
     * Feature: smart-campus-lost-found, Property 15: 积分排行榜排序正确性
     * 测试: 对于任意两条相邻排名，前一名的points应>=后一名
     * Validates: Requirements 7.5
     */
    @Property(tries = 100)
    void adjacentRanksShouldHaveCorrectPointsRelation(
            @ForAll("validLimits") Integer limit) {
        
        // Setup mocks
        PointRecordMapper pointRecordMapper = mock(PointRecordMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        ObjectMapper objectMapper = new ObjectMapper();
        CacheService cacheService = mock(CacheService.class);
        
        PointServiceImpl pointService = new PointServiceImpl(pointRecordMapper, userMapper, redisUtil, objectMapper, cacheService);
        
        // Create mock users with random points (sorted by points desc)
        List<User> mockUsers = createMockUsersForRanking(limit);
        
        when(userMapper.selectList(any())).thenReturn(mockUsers);
        
        // Execute: get ranking
        List<PointRankVO> ranking = pointService.getRanking(limit);
        
        // Verify: for any two adjacent ranks, the higher rank should have >= points
        for (int i = 0; i < ranking.size() - 1; i++) {
            PointRankVO higherRank = ranking.get(i);
            PointRankVO lowerRank = ranking.get(i + 1);
            
            // Verify rank numbers are sequential
            assertEquals(i + 1, higherRank.getRank(),
                "Rank number should be sequential starting from 1");
            assertEquals(i + 2, lowerRank.getRank(),
                "Rank number should be sequential");
            
            // Verify points relationship
            assertTrue(higherRank.getPoints() >= lowerRank.getPoints(),
                String.format("Rank %d (points=%d) should have >= points than rank %d (points=%d)",
                    higherRank.getRank(), higherRank.getPoints(),
                    lowerRank.getRank(), lowerRank.getPoints()));
        }
    }

    /**
     * Property 15: 积分排行榜排序正确性 - 排名序号正确性
     * 
     * Feature: smart-campus-lost-found, Property 15: 积分排行榜排序正确性
     * 测试: 排行榜中的排名序号应从1开始连续递增
     * Validates: Requirements 7.5
     */
    @Property(tries = 100)
    void rankNumbersShouldBeSequential(
            @ForAll("validLimits") Integer limit) {
        
        // Setup mocks
        PointRecordMapper pointRecordMapper = mock(PointRecordMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        ObjectMapper objectMapper = new ObjectMapper();
        CacheService cacheService = mock(CacheService.class);
        
        PointServiceImpl pointService = new PointServiceImpl(pointRecordMapper, userMapper, redisUtil, objectMapper, cacheService);
        
        // Create mock users
        List<User> mockUsers = createMockUsersForRanking(limit);
        
        when(userMapper.selectList(any())).thenReturn(mockUsers);
        
        // Execute: get ranking
        List<PointRankVO> ranking = pointService.getRanking(limit);
        
        // Verify: rank numbers are sequential starting from 1
        for (int i = 0; i < ranking.size(); i++) {
            assertEquals(i + 1, ranking.get(i).getRank(),
                String.format("Rank at position %d should be %d, but was %d",
                    i, i + 1, ranking.get(i).getRank()));
        }
    }

    // ==================== Additional Generators for Property 15 ====================

    /**
     * Generator for valid limit values (1 to 100)
     */
    @Provide
    Arbitrary<Integer> validLimits() {
        return Arbitraries.integers().between(1, 100);
    }

    /**
     * Generator for limit values that may exceed 100 (to test the cap)
     */
    @Provide
    Arbitrary<Integer> limitsUpTo200() {
        return Arbitraries.integers().between(1, 200);
    }

    /**
     * Helper method to create mock users for ranking tests
     * Returns users sorted by points descending (simulating DB ORDER BY)
     */
    private List<User> createMockUsersForRanking(int count) {
        List<User> users = new ArrayList<>();
        int actualCount = Math.min(count, 100); // Cap at 100
        
        // Create users with decreasing points to simulate sorted result
        for (int i = 0; i < actualCount; i++) {
            User user = new User();
            user.setId((long) (i + 1));
            user.setName("User" + (i + 1));
            user.setAvatar("avatar" + (i + 1) + ".jpg");
            user.setStatus(0); // Normal status
            // Points decrease as rank increases (higher rank = more points)
            user.setPoints(1000 - (i * 10) + (int)(Math.random() * 5));
            users.add(user);
        }
        
        // Sort by points descending to simulate DB behavior
        users.sort((a, b) -> b.getPoints().compareTo(a.getPoints()));
        
        return users;
    }
}
