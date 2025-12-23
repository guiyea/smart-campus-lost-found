package com.campus.lostandfound.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物品搜索DTO
 */
@Data
public class ItemSearchDTO {
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 类型筛选：0失物 1招领
     */
    private Integer type;
    
    /**
     * 类别筛选
     */
    private String category;
    
    /**
     * 状态筛选：0待处理 1已找回 2已关闭
     */
    private Integer status;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 经度（用于地理范围搜索）
     */
    private BigDecimal longitude;
    
    /**
     * 纬度（用于地理范围搜索）
     */
    private BigDecimal latitude;
    
    /**
     * 搜索半径（米）
     */
    private Integer radius;
    
    /**
     * 排序方式：time/distance/match
     */
    private String sortBy;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 20;
}
