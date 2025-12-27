package com.campus.lostandfound.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息VO
 */
@Schema(description = "用户信息")
@Data
public class UserVO {
    
    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    /**
     * 学号/工号
     */
    @Schema(description = "学号/工号", example = "2021001001", accessMode = Schema.AccessMode.READ_ONLY)
    private String studentId;
    
    /**
     * 姓名
     */
    @Schema(description = "姓名", example = "张三", accessMode = Schema.AccessMode.READ_ONLY)
    private String name;
    
    /**
     * 手机号（脱敏）
     */
    @Schema(description = "手机号（脱敏）", example = "138****8000", accessMode = Schema.AccessMode.READ_ONLY)
    private String phone;
    
    /**
     * 头像URL
     */
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg", accessMode = Schema.AccessMode.READ_ONLY)
    private String avatar;
    
    /**
     * 积分
     */
    @Schema(description = "积分", example = "100", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer points;
    
    /**
     * 角色：0-普通用户，1-管理员
     */
    @Schema(description = "角色：0-普通用户，1-管理员", example = "0", allowableValues = {"0", "1"}, accessMode = Schema.AccessMode.READ_ONLY)
    private Integer role;
    
    /**
     * 状态：0-正常，1-封禁
     */
    @Schema(description = "状态：0-正常，1-封禁", example = "0", allowableValues = {"0", "1"}, accessMode = Schema.AccessMode.READ_ONLY)
    private Integer status;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-01-15 10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * 脱敏手机号
     * 将手机号中间4位替换为****
     */
    public void maskPhone() {
        if (phone != null && phone.length() == 11) {
            this.phone = phone.substring(0, 3) + "****" + phone.substring(7);
        }
    }
}
