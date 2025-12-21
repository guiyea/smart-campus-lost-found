package com.campus.lostandfound.exception;

/**
 * 无权限异常
 * HTTP状态码: 403
 * 用于处理用户已认证但无权限访问资源的情况
 */
public class ForbiddenException extends BusinessException {
    
    public ForbiddenException(String message) {
        super(403, message);
    }
    
    public ForbiddenException() {
        super(403, "无权限访问该资源");
    }
}
