package com.campus.lostandfound.service;

/**
 * 缓存服务接口
 * 提供统一的缓存管理方法
 */
public interface CacheService {
    
    /**
     * 清除物品详情缓存
     * @param itemId 物品ID
     */
    void evictItemDetailCache(Long itemId);
    
    /**
     * 清除所有物品搜索缓存
     */
    void evictAllItemSearchCache();
    
    /**
     * 清除用户信息缓存
     * @param userId 用户ID
     */
    void evictUserInfoCache(Long userId);
    
    /**
     * 清除积分排行榜缓存
     */
    void evictPointRankingCache();
    
    /**
     * 更新积分排行榜中用户的分数
     * @param userId 用户ID
     * @param points 增加的积分
     */
    void updatePointRankingScore(Long userId, Integer points);
    
    /**
     * 清除物品相关的所有缓存（详情+搜索）
     * @param itemId 物品ID
     */
    void evictItemRelatedCache(Long itemId);
}
