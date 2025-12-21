package com.campus.lostandfound.model.vo;

import lombok.Data;

/**
 * 认证令牌VO
 */
@Data
public class TokenVO {
    
    /**
     * 访问令牌
     */
    private String accessToken;
    
    /**
     * 刷新令牌
     */
    private String refreshToken;
    
    /**
     * 访问令牌过期时间（秒）
     */
    private Long expiresIn;
    
    /**
     * 用户信息
     */
    private UserVO userInfo;
}
