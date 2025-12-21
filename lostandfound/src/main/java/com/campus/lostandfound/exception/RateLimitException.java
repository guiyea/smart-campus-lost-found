package com.campus.lostandfound.exception;

/**
 * 请求频率超限异常
 * HTTP状态码: 429
 * 用于处理请求频率超过限制的情况
 */
public class RateLimitException extends BusinessException {
    
    public RateLimitException(String message) {
        super(429, message);
    }
    
    public RateLimitException() {
        super(429, "请求过于频繁，请稍后再试");
    }
}
