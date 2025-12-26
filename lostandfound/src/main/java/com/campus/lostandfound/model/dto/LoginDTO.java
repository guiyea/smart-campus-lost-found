package com.campus.lostandfound.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录DTO
 */
@Schema(description = "用户登录信息")
@Data
public class LoginDTO {
    
    /**
     * 学号/工号
     */
    @Schema(description = "学号/工号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2021001001")
    @NotBlank(message = "学号不能为空")
    private String studentId;
    
    /**
     * 密码
     */
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;
}
