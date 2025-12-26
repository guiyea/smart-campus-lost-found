package com.campus.lostandfound.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册DTO
 */
@Schema(description = "用户注册信息")
@Data
public class RegisterDTO {
    
    /**
     * 学号/工号
     */
    @Schema(description = "学号/工号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2021001001", minLength = 5, maxLength = 20)
    @NotBlank(message = "学号不能为空")
    @Size(min = 5, max = 20, message = "学号长度必须在5-20个字符之间")
    private String studentId;
    
    /**
     * 姓名
     */
    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三", minLength = 2, maxLength = 20)
    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 20, message = "姓名长度必须在2-20个字符之间")
    private String name;
    
    /**
     * 手机号
     */
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000", pattern = "^1[3-9]\\d{9}$")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 密码
     */
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456", minLength = 6, maxLength = 20)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;
}
