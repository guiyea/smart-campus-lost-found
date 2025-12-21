package com.campus.lostandfound.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知实体类
 */
@Data
@TableName("message")
public class Message {
    
    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 接收用户ID
     */
    private Long userId;
    
    /**
     * 消息标题
     */
    private String title;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型: 0-系统通知, 1-匹配通知, 2-留言通知
     */
    private Integer type;
    
    /**
     * 关联ID(物品ID/匹配ID等)
     */
    private Long relatedId;
    
    /**
     * 已读标记: 0-未读, 1-已读
     */
    private Integer isRead;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
