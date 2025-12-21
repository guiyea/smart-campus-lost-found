package com.campus.lostandfound.exception;

import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.common.ResultCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理系统中的各类异常，返回标准化的错误响应
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     * 返回对应的业务错误码和消息
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        logger.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理方法参数验证异常（@Valid注解触发）
     * 提取字段验证错误信息，返回422
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        
        logger.warn("参数验证失败: {}", errorMessage);
        return Result.error(ResultCode.VALIDATION_ERROR.getCode(), errorMessage);
    }

    /**
     * 处理约束违反异常（@Validated注解触发）
     * 提取参数验证错误信息，返回422
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(violation -> {
                    String propertyPath = violation.getPropertyPath().toString();
                    // 提取参数名（去掉方法名前缀）
                    String fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
                    return fieldName + ": " + violation.getMessage();
                })
                .collect(Collectors.joining("; "));
        
        logger.warn("参数约束验证失败: {}", errorMessage);
        return Result.error(ResultCode.VALIDATION_ERROR.getCode(), errorMessage);
    }

    /**
     * 处理HTTP消息不可读异常（请求体格式错误）
     * 返回400请求体格式错误
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        logger.warn("请求体格式错误: {}", e.getMessage());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), "请求体格式错误，请检查JSON格式");
    }

    /**
     * 处理404异常（找不到处理器）
     * 返回404资源不存在
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        logger.warn("请求路径不存在: {}", e.getRequestURL());
        return Result.error(ResultCode.NOT_FOUND.getCode(), "请求的接口不存在");
    }

    /**
     * 处理所有未捕获的异常
     * 记录错误日志（不泄露敏感信息），返回500
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        // 记录完整的异常堆栈信息到日志，但不返回给客户端
        logger.error("系统内部错误", e);
        
        // 返回通用错误信息，不泄露敏感信息
        return Result.error(ResultCode.INTERNAL_ERROR.getCode(), ResultCode.INTERNAL_ERROR.getMessage());
    }
}
