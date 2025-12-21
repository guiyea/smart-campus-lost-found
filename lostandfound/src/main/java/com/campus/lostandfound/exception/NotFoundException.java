package com.campus.lostandfound.exception;

/**
 * 资源不存在异常
 * HTTP状态码: 404
 * 用于处理请求的资源不存在的情况
 */
public class NotFoundException extends BusinessException {
    
    public NotFoundException(String message) {
        super(404, message);
    }
    
    public NotFoundException() {
        super(404, "请求的资源不存在");
    }
}
