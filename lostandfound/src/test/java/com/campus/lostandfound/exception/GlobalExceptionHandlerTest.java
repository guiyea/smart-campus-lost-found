package com.campus.lostandfound.exception;

import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.common.ResultCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 全局异常处理器测试
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleBusinessException() {
        // Given
        BusinessException exception = new BusinessException(400, "业务错误");

        // When
        Result<Void> result = handler.handleBusinessException(exception);

        // Then
        assertNotNull(result);
        assertEquals(400, result.getCode());
        assertEquals("业务错误", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testHandleUnauthorizedException() {
        // Given
        UnauthorizedException exception = new UnauthorizedException("未登录");

        // When
        Result<Void> result = handler.handleBusinessException(exception);

        // Then
        assertNotNull(result);
        assertEquals(401, result.getCode());
        assertEquals("未登录", result.getMessage());
    }

    @Test
    void testHandleForbiddenException() {
        // Given
        ForbiddenException exception = new ForbiddenException("无权限");

        // When
        Result<Void> result = handler.handleBusinessException(exception);

        // Then
        assertNotNull(result);
        assertEquals(403, result.getCode());
        assertEquals("无权限", result.getMessage());
    }

    @Test
    void testHandleNotFoundException() {
        // Given
        NotFoundException exception = new NotFoundException("资源不存在");

        // When
        Result<Void> result = handler.handleBusinessException(exception);

        // Then
        assertNotNull(result);
        assertEquals(404, result.getCode());
        assertEquals("资源不存在", result.getMessage());
    }

    @Test
    void testHandleValidationException() {
        // Given
        ValidationException exception = new ValidationException("参数验证失败");

        // When
        Result<Void> result = handler.handleBusinessException(exception);

        // Then
        assertNotNull(result);
        assertEquals(422, result.getCode());
        assertEquals("参数验证失败", result.getMessage());
    }

    @Test
    void testHandleRateLimitException() {
        // Given
        RateLimitException exception = new RateLimitException("请求频率超限");

        // When
        Result<Void> result = handler.handleBusinessException(exception);

        // Then
        assertNotNull(result);
        assertEquals(429, result.getCode());
        assertEquals("请求频率超限", result.getMessage());
    }

    @Test
    void testHandleMethodArgumentNotValidException() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError1 = new FieldError("user", "name", "不能为空");
        FieldError fieldError2 = new FieldError("user", "phone", "格式不正确");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // When
        Result<Void> result = handler.handleMethodArgumentNotValidException(exception);

        // Then
        assertNotNull(result);
        assertEquals(422, result.getCode());
        assertTrue(result.getMessage().contains("name: 不能为空"));
        assertTrue(result.getMessage().contains("phone: 格式不正确"));
    }

    @Test
    void testHandleHttpMessageNotReadableException() {
        // Given
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        when(exception.getMessage()).thenReturn("JSON parse error");

        // When
        Result<Void> result = handler.handleHttpMessageNotReadableException(exception);

        // Then
        assertNotNull(result);
        assertEquals(400, result.getCode());
        assertEquals("请求体格式错误，请检查JSON格式", result.getMessage());
    }

    @Test
    void testHandleNoHandlerFoundException() {
        // Given
        NoHandlerFoundException exception = mock(NoHandlerFoundException.class);
        when(exception.getRequestURL()).thenReturn("/api/v1/nonexistent");

        // When
        Result<Void> result = handler.handleNoHandlerFoundException(exception);

        // Then
        assertNotNull(result);
        assertEquals(404, result.getCode());
        assertEquals("请求的接口不存在", result.getMessage());
    }

    @Test
    void testHandleGenericException() {
        // Given
        Exception exception = new RuntimeException("未知错误");

        // When
        Result<Void> result = handler.handleException(exception);

        // Then
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("系统内部错误", result.getMessage());
        assertNull(result.getData());
    }
}
