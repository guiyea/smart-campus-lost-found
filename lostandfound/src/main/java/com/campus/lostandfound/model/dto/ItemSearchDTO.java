package com.campus.lostandfound.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物品搜索DTO
 */
@Schema(description = "物品搜索条件")
@Data
public class ItemSearchDTO {
    
    /**
     * 搜索关键词
     */
    @Schema(description = "搜索关键词", example = "iPhone")
    private String keyword;
    
    /**
     * 类型筛选：0失物 1招领
     */
    @Schema(description = "类型筛选：0-失物, 1-招领", example = "0", allowableValues = {"0", "1"})
    private Integer type;
    
    /**
     * 类别筛选
     */
    @Schema(description = "类别筛选", example = "电子设备")
    private String category;
    
    /**
     * 状态筛选：0待处理 1已找回 2已关闭
     */
    @Schema(description = "状态筛选：0-待处理, 1-已找回, 2-已关闭", example = "0", allowableValues = {"0", "1", "2"})
    private Integer status;
    
    /**
     * 开始时间
     */
    @Schema(description = "开始时间", example = "2024-01-01T00:00:00")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @Schema(description = "结束时间", example = "2024-12-31T23:59:59")
    private LocalDateTime endTime;
    
    /**
     * 经度（用于地理范围搜索）
     */
    @Schema(description = "经度（用于地理范围搜索）", example = "121.4737")
    private BigDecimal longitude;
    
    /**
     * 纬度（用于地理范围搜索）
     */
    @Schema(description = "纬度（用于地理范围搜索）", example = "31.2304")
    private BigDecimal latitude;
    
    /**
     * 搜索半径（米）
     */
    @Schema(description = "搜索半径（米）", example = "1000")
    private Integer radius;
    
    /**
     * 排序方式：time/distance/match
     */
    @Schema(description = "排序方式：time-时间, distance-距离, match-匹配度", example = "time", allowableValues = {"time", "distance", "match"})
    private String sortBy;
    
    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;
}
