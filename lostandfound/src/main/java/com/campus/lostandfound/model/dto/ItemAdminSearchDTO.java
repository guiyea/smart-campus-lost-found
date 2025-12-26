package com.campus.lostandfound.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员物品搜索DTO
 * 用于管理员查询所有物品（包括已删除的）
 */
@Schema(description = "管理员物品搜索条件")
@Data
public class ItemAdminSearchDTO {
    
    /**
     * 搜索关键词（标题、描述）
     */
    @Schema(description = "搜索关键词（标题、描述）", example = "iPhone")
    private String keyword;
    
    /**
     * 类型筛选：0失物 1招领
     */
    @Schema(description = "类型筛选：0-失物, 1-招领", example = "0", allowableValues = {"0", "1"})
    private Integer type;
    
    /**
     * 状态筛选：0待处理 1已找回 2已关闭
     */
    @Schema(description = "状态筛选：0-待处理, 1-已找回, 2-已关闭", example = "0", allowableValues = {"0", "1", "2"})
    private Integer status;
    
    /**
     * 删除状态筛选：0未删除 1已删除，null表示查询所有
     */
    @Schema(description = "删除状态筛选：0-未删除, 1-已删除，null表示查询所有", example = "0", allowableValues = {"0", "1"})
    private Integer deleted;
    
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
     * 举报次数筛选（大于等于该值）
     */
    @Schema(description = "举报次数筛选（大于等于该值）", example = "3")
    private Integer reportCount;
    
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
