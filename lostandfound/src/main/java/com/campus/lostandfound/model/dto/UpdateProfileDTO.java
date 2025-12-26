package com.campus.lostandfound.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户信息更新DTO
 * 所有字段可选，只更新非空字段
 */
@Schema(description = "用户信息更新")
@Data
public class UpdateProfileDTO {
    
    /**
     * 姓名
     */
    @Schema(description = "姓名", example = "张三", minLength = 2, maxLength = 20)
    @Size(min = 2, max = 20, message = "姓名长度必须在2-20个字符之间")
    private String name;
    
    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138000", pattern = "^1[3-9]\\d{9}$")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 头像URL
     */
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
    
    /**
     * 旧密码（修改密码时需要）
     */
    @Schema(description = "旧密码（修改密码时需要）", example = "oldpass123")
    private String oldPassword;
    
    /**
     * 新密码
     */
    @Schema(description = "新密码", example = "newpass123", minLength = 6, maxLength = 20)
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String newPassword;
}
