package com.campus.lostandfound.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 认证令牌VO
 */
@Schema(description = "认证令牌信息")
@Data
public class TokenVO {
    
    /**
     * 访问令牌
     */
    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", accessMode = Schema.AccessMode.READ_ONLY)
    private String accessToken;
    
    /**
     * 刷新令牌
     */
    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", accessMode = Schema.AccessMode.READ_ONLY)
    private String refreshToken;
    
    /**
     * 访问令牌过期时间（秒）
     */
    @Schema(description = "访问令牌过期时间（秒）", example = "7200", accessMode = Schema.AccessMode.READ_ONLY)
    private Long expiresIn;
    
    /**
     * 用户信息
     */
    @Schema(description = "用户信息", accessMode = Schema.AccessMode.READ_ONLY)
    private UserVO userInfo;
}
