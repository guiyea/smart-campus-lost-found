package com.campus.lostandfound.common;

/**
 * 统一响应状态码枚举
 */
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    VALIDATION_ERROR(422, "参数验证失败"),
    RATE_LIMIT(429, "请求频率超限"),
    INTERNAL_ERROR(500, "系统内部错误");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
