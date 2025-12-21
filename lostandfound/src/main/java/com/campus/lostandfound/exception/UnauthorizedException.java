package com.campus.lostandfound.exception;

/**
 * 未认证异常
 * HTTP状态码: 401
 * 用于处理用户未登录或令牌无效的情况
 */
public class UnauthorizedException extends BusinessException {
    
    public UnauthorizedException(String message) {
        super(401, message);
    }
    
    public UnauthorizedException() {
        super(401, "未认证，请先登录");
    }
}
