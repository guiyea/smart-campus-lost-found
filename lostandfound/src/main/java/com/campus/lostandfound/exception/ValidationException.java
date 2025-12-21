package com.campus.lostandfound.exception;

/**
 * 参数验证异常
 * HTTP状态码: 422
 * 用于处理请求参数验证失败的情况
 */
public class ValidationException extends BusinessException {
    
    public ValidationException(String message) {
        super(422, message);
    }
    
    public ValidationException() {
        super(422, "参数验证失败");
    }
}
