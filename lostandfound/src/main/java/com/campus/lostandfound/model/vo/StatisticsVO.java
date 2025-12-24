package com.campus.lostandfound.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 平台统计数据VO
 */
@Data
public class StatisticsVO {
    
    /**
     * 用户总数
     */
    private Long totalUsers;
    
    /**
     * 信息总数
     */
    private Long totalItems;
    
    /**
     * 失物信息数
     */
    private Long totalLostItems;
    
    /**
     * 招领信息数
     */
    private Long totalFoundItems;
    
    /**
     * 匹配成功数
     */
    private Long totalMatched;
    
    /**
     * 匹配成功率
     */
    private BigDecimal matchRate;
    
    /**
     * 今日新增用户
     */
    private Integer todayNewUsers;
    
    /**
     * 今日新增信息
     */
    private Integer todayNewItems;
    
    /**
     * 近7天数据趋势
     */
    private List<DailyStatVO> weeklyTrend;
}
