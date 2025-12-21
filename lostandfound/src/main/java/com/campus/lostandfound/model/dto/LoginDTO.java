package com.campus.lostandfound.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录DTO
 */
@Data
public class LoginDTO {
    
    /**
     * 学号/工号
     */
    @NotBlank(message = "学号不能为空")
    private String studentId;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
