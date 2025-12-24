package com.campus.lostandfound.model.vo;

import lombok.Data;

/**
 * 积分排行榜VO
 */
@Data
public class PointRankVO {
    
    /**
     * 排名
     */
    private Integer rank;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户姓名
     */
    private String userName;
    
    /**
     * 用户头像URL
     */
    private String userAvatar;
    
    /**
     * 积分总数
     */
    private Integer points;
}
