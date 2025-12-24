package com.campus.lostandfound.service.impl;

import com.campus.lostandfound.config.RedisCacheConfig;
import com.campus.lostandfound.service.CacheService;
import com.campus.lostandfound.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 缓存服务实现类
 * 提供统一的缓存管理方法，使用@CacheEvict注解简化缓存操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {
    
    private static final String POINT_RANKING_KEY = "point:ranking";
    private static final String ITEM_DETAIL_CACHE_KEY = "item:detail:";
    private static final String ITEM_SEARCH_CACHE_KEY = "item:search:";
    private static final String USER_INFO_CACHE_KEY = "user:info:";
    
    private final RedisUtil redisUtil;
    
    /**
     * 清除物品详情缓存
     * 使用@CacheEvict注解自动清除Spring Cache管理的缓存
     */
    @Override
    @CacheEvict(value = RedisCacheConfig.CACHE_ITEM_DETAIL, key = "#itemId")
    public void evictItemDetailCache(Long itemId) {
        // 同时清除手动管理的Redis缓存
        String cacheKey = ITEM_DETAIL_CACHE_KEY + itemId;
        redisUtil.delete(cacheKey);
        log.info("已清除物品详情缓存: itemId={}", itemId);
    }
    
    /**
     * 清除所有物品搜索缓存
     * 使用@CacheEvict注解的allEntries属性清除所有搜索缓存
     */
    @Override
    @CacheEvict(value = RedisCacheConfig.CACHE_ITEM_SEARCH, allEntries = true)
    public void evictAllItemSearchCache() {
        // 同时清除手动管理的Redis缓存
        Set<String> searchKeys = redisUtil.keys(ITEM_SEARCH_CACHE_KEY + "*");
        if (searchKeys != null && !searchKeys.isEmpty()) {
            redisUtil.delete(searchKeys);
            log.info("已清除 {} 个搜索结果缓存", searchKeys.size());
        }
    }
    
    /**
     * 清除用户信息缓存
     * 使用@CacheEvict注解自动清除Spring Cache管理的缓存
     */
    @Override
    @CacheEvict(value = RedisCacheConfig.CACHE_USER_INFO, key = "#userId")
    public void evictUserInfoCache(Long userId) {
        // 同时清除手动管理的Redis缓存
        String cacheKey = USER_INFO_CACHE_KEY + userId;
        redisUtil.delete(cacheKey);
        log.info("已清除用户信息缓存: userId={}", userId);
    }
    
    /**
     * 清除积分排行榜缓存
     * 使用@CacheEvict注解的allEntries属性清除所有排行榜缓存
     */
    @Override
    @CacheEvict(value = RedisCacheConfig.CACHE_POINT_RANKING, allEntries = true)
    public void evictPointRankingCache() {
        // 同时清除手动管理的Redis ZSET缓存
        redisUtil.delete(POINT_RANKING_KEY);
        log.info("已清除积分排行榜缓存");
    }
    
    /**
     * 更新积分排行榜中用户的分数
     * 使用Redis ZSET的原子操作更新分数
     */
    @Override
    public void updatePointRankingScore(Long userId, Integer points) {
        // 如果排行榜缓存存在，更新ZSET中的分数
        if (Boolean.TRUE.equals(redisUtil.hasKey(POINT_RANKING_KEY))) {
            redisUtil.zIncrementScore(POINT_RANKING_KEY, String.valueOf(userId), points);
            log.info("已更新排行榜缓存分数: userId={}, 增加积分={}", userId, points);
        }
    }
    
    /**
     * 清除物品相关的所有缓存（详情+搜索）
     * 使用@Caching注解组合多个缓存清除操作
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = RedisCacheConfig.CACHE_ITEM_DETAIL, key = "#itemId"),
            @CacheEvict(value = RedisCacheConfig.CACHE_ITEM_SEARCH, allEntries = true)
    })
    public void evictItemRelatedCache(Long itemId) {
        // 同时清除手动管理的Redis缓存
        String detailCacheKey = ITEM_DETAIL_CACHE_KEY + itemId;
        redisUtil.delete(detailCacheKey);
        log.info("已清除物品详情缓存: itemId={}", itemId);
        
        Set<String> searchKeys = redisUtil.keys(ITEM_SEARCH_CACHE_KEY + "*");
        if (searchKeys != null && !searchKeys.isEmpty()) {
            redisUtil.delete(searchKeys);
            log.info("已清除 {} 个搜索结果缓存", searchKeys.size());
        }
    }
}
