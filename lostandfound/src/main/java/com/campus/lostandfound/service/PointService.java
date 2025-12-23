package com.campus.lostandfound.service;

/**
 * 积分服务接口
 */
public interface PointService {
    
    /**
     * 增加用户积分
     * 
     * @param userId 用户ID
     * @param points 积分数量
     * @param reason 原因
     * @param relatedId 关联ID
     */
    void addPoints(Long userId, Integer points, String reason, Long relatedId);
}
