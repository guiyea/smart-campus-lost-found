package com.campus.lostandfound.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 匹配反馈DTO
 */
@Data
public class MatchFeedbackDTO {
    
    /**
     * 物品ID
     */
    @NotNull(message = "物品ID不能为空")
    private Long itemId;
    
    /**
     * 匹配的物品ID
     */
    @NotNull(message = "匹配物品ID不能为空")
    private Long matchedItemId;
    
    /**
     * 反馈用户ID（通常从JWT令牌中获取）
     */
    private Long userId;
    
    /**
     * 是否准确匹配
     */
    @NotNull(message = "匹配准确性不能为空")
    private Boolean isAccurate;
    
    /**
     * 反馈评论
     */
    private String comment;
}