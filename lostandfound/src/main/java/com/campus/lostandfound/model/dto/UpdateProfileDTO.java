package com.campus.lostandfound.model.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户信息更新DTO
 * 所有字段可选，只更新非空字段
 */
@Data
public class UpdateProfileDTO {
    
    /**
     * 姓名
     */
    @Size(min = 2, max = 20, message = "姓名长度必须在2-20个字符之间")
    private String name;
    
    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 旧密码（修改密码时需要）
     */
    private String oldPassword;
    
    /**
     * 新密码
     */
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String newPassword;
}
