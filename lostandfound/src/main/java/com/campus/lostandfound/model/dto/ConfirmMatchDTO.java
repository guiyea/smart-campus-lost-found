package com.campus.lostandfound.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 确认匹配DTO
 */
@Data
public class ConfirmMatchDTO {
    
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
}