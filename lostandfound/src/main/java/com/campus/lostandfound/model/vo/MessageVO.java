package com.campus.lostandfound.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知VO
 */
@Schema(description = "消息通知信息")
@Data
public class MessageVO {
    
    /**
     * 消息ID
     */
    @Schema(description = "消息ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    /**
     * 消息标题
     */
    @Schema(description = "消息标题", example = "匹配成功通知", accessMode = Schema.AccessMode.READ_ONLY)
    private String title;
    
    /**
     * 消息内容
     */
    @Schema(description = "消息内容", example = "您发布的物品已成功匹配", accessMode = Schema.AccessMode.READ_ONLY)
    private String content;
    
    /**
     * 消息类型: 0-系统通知, 1-匹配通知, 2-留言通知
     */
    @Schema(description = "消息类型：0-系统通知, 1-匹配通知, 2-留言通知", example = "1", allowableValues = {"0", "1", "2"}, accessMode = Schema.AccessMode.READ_ONLY)
    private Integer type;
    
    /**
     * 消息类型名称
     */
    @Schema(description = "消息类型名称", example = "匹配通知", accessMode = Schema.AccessMode.READ_ONLY)
    private String typeName;
    
    /**
     * 关联ID(物品ID/匹配ID等)
     */
    @Schema(description = "关联ID(物品ID/匹配ID等)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long relatedId;
    
    /**
     * 已读标记: false-未读, true-已读
     */
    @Schema(description = "已读标记：false-未读, true-已读", example = "false", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean isRead;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-01-15 10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
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