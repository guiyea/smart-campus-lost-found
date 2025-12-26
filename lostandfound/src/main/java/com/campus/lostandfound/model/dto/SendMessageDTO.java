package com.campus.lostandfound.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发送消息DTO
 */
@Schema(description = "发送消息")
@Data
public class SendMessageDTO {
    
    /**
     * 接收用户ID
     */
    @Schema(description = "接收用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "接收用户ID不能为空")
    private Long userId;
    
    /**
     * 消息标题
     */
    @Schema(description = "消息标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "匹配成功通知")
    @NotBlank(message = "消息标题不能为空")
    private String title;
    
    /**
     * 消息内容
     */
    @Schema(description = "消息内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "您发布的物品已成功匹配")
    @NotBlank(message = "消息内容不能为空")
    private String content;
    
    /**
     * 消息类型: 0-系统通知, 1-匹配通知, 2-留言通知
     */
    @Schema(description = "消息类型：0-系统通知, 1-匹配通知, 2-留言通知", requiredMode = Schema.RequiredMode.REQUIRED, example = "1", allowableValues = {"0", "1", "2"})
    @NotNull(message = "消息类型不能为空")
    private Integer type;
    
    /**
     * 关联ID(物品ID/匹配ID等)
     */
    @Schema(description = "关联ID(物品ID/匹配ID等)", example = "1")
    private Long relatedId;
}