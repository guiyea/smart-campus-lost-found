package com.campus.lostandfound.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员物品搜索DTO
 * 用于管理员查询所有物品（包括已删除的）
 */
@Data
public class ItemAdminSearchDTO {
    
    /**
     * 搜索关键词（标题、描述）
     */
    private String keyword;
    
    /**
     * 类型筛选：0失物 1招领
     */
    private Integer type;
    
    /**
     * 状态筛选：0待处理 1已找回 2已关闭
     */
    private Integer status;
    
    /**
     * 删除状态筛选：0未删除 1已删除，null表示查询所有
     */
    private Integer deleted;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 举报次数筛选（大于等于该值）
     */
    private Integer reportCount;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 20;
}
