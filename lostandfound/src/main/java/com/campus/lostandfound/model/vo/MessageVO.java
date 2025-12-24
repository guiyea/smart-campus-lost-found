package com.campus.lostandfound.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知VO
 */
@Data
public class MessageVO {
    
    /**
     * 消息ID
     */
    private Long id;
    
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
     * 消息类型名称
     */
    private String typeName;
    
    /**
     * 关联ID(物品ID/匹配ID等)
     */
    private Long relatedId;
    
    /**
     * 已读标记: false-未读, true-已读
     */
    private Boolean isRead;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * 根据消息类型设置类型名称
     */
    public void setTypeNameByType() {
        if (type != null) {
            switch (type) {
                case 0:
                    this.typeName = "系统通知";
                    break;
                case 1:
                    this.typeName = "匹配通知";
                    break;
                case 2:
                    this.typeName = "留言通知";
                    break;
                default:
                    this.typeName = "未知类型";
                    break;
            }
        }
    }
}