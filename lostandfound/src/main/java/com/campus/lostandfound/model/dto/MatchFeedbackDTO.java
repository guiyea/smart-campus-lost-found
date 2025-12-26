package com.campus.lostandfound.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 匹配反馈DTO
 */
@Schema(description = "匹配反馈")
@Data
public class MatchFeedbackDTO {
    
    /**
     * 物品ID
     */
    @Schema(description = "物品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "物品ID不能为空")
    private Long itemId;
    
    /**
     * 匹配的物品ID
     */
    @Schema(description = "匹配的物品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "匹配物品ID不能为空")
    private Long matchedItemId;
    
    /**
     * 反馈用户ID（通常从JWT令牌中获取）
     */
    @Schema(description = "反馈用户ID（通常从JWT令牌中获取）", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;
    
    /**
     * 是否准确匹配
     */
    @Schema(description = "是否准确匹配", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "匹配准确性不能为空")
    private Boolean isAccurate;
    
    /**
     * 反馈评论
     */
    @Schema(description = "反馈评论", example = "匹配准确，已成功找回")
    private String comment;
}