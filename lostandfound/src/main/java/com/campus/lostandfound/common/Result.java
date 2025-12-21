package com.campus.lostandfound.common;

/**
 * 统一响应结果类
 * @param <T> 响应数据类型
 */
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public Result() {
    }

    public Result(Integer code, String message, T data, Long timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(
            ResultCode.SUCCESS.getCode(),
            ResultCode.SUCCESS.getMessage(),
            null,
            System.currentTimeMillis()
        );
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(
            ResultCode.SUCCESS.getCode(),
            ResultCode.SUCCESS.getMessage(),
            data,
            System.currentTimeMillis()
        );
    }

    /**
     * 成功响应（自定义消息和数据）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(
            ResultCode.SUCCESS.getCode(),
            message,
            data,
            System.currentTimeMillis()
        );
    }

    /**
     * 错误响应（指定状态码和消息）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(
            code,
            message,
            null,
            System.currentTimeMillis()
        );
    }

    /**
     * 错误响应（使用ResultCode枚举）
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(
            resultCode.getCode(),
            resultCode.getMessage(),
            null,
            System.currentTimeMillis()
        );
    }

    // Getters and Setters
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
