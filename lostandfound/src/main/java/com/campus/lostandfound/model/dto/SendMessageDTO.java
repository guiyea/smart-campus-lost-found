package com.campus.lostandfound.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发送消息DTO
 */
@Data
public class SendMessageDTO {
    
    /**
     * 接收用户ID
     */
    @NotNull(message = "接收用户ID不能为空")
    private Long userId;
    
    /**
     * 消息标题
     */
    @NotBlank(message = "消息标题不能为空")
    private String title;
    
    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;
    
    /**
     * 消息类型: 0-系统通知, 1-匹配通知, 2-留言通知
     */
    @NotNull(message = "消息类型不能为空")
    private Integer type;
    
    /**
     * 关联ID(物品ID/匹配ID等)
     */
    private Long relatedId;
}