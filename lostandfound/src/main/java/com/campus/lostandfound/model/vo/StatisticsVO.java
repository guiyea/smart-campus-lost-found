package com.campus.lostandfound.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 平台统计数据VO
 */
@Schema(description = "平台统计数据")
@Data
public class StatisticsVO {
    
    /**
     * 用户总数
     */
    @Schema(description = "用户总数", example = "1000", accessMode = Schema.AccessMode.READ_ONLY)
    private Long totalUsers;
    
    /**
     * 信息总数
     */
    @Schema(description = "信息总数", example = "5000", accessMode = Schema.AccessMode.READ_ONLY)
    private Long totalItems;
    
    /**
     * 失物信息数
     */
    @Schema(description = "失物信息数", example = "3000", accessMode = Schema.AccessMode.READ_ONLY)
    private Long totalLostItems;
    
    /**
     * 招领信息数
     */
    @Schema(description = "招领信息数", example = "2000", accessMode = Schema.AccessMode.READ_ONLY)
    private Long totalFoundItems;
    
    /**
     * 匹配成功数
     */
    @Schema(description = "匹配成功数", example = "1500", accessMode = Schema.AccessMode.READ_ONLY)
    private Long totalMatched;
    
    /**
     * 匹配成功率
     */
    @Schema(description = "匹配成功率", example = "30.00", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal matchRate;
    
    /**
     * 今日新增用户
     */
    @Schema(description = "今日新增用户", example = "10", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer todayNewUsers;
    
    /**
     * 今日新增信息
     */
    @Schema(description = "今日新增信息", example = "50", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer todayNewItems;
    
    /**
     * 近7天数据趋势
     */
    @Schema(description = "近7天数据趋势", accessMode = Schema.AccessMode.READ_ONLY)
    private List<DailyStatVO> weeklyTrend;
}
