package com.campus.lostandfound.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 确认匹配DTO
 */
@Schema(description = "确认匹配")
@Data
public class ConfirmMatchDTO {
    
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
}