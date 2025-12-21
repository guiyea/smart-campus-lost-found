package com.campus.lostandfound.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil单元测试
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // 使用反射设置私有字段值
        ReflectionTestUtils.setField(jwtUtil, "secretKey", 
            "your-secret-key-change-this-in-production-at-least-256-bits-long");
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", 7200000L); // 2小时
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpiration", 604800000L); // 7天
    }

    @Test
    void testGenerateAccessToken() {
        // Given
        Long userId = 1L;
        String studentId = "2021001";
        Integer role = 0;

        // When
        String token = jwtUtil.generateAccessToken(userId, studentId, role);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT格式验证
    }

    @Test
    void testGenerateRefreshToken() {
        // Given
        Long userId = 1L;

        // When
        String token = jwtUtil.generateRefreshToken(userId);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void testParseToken() {
        // Given
        Long userId = 1L;
        String studentId = "2021001";
        Integer role = 0;
        String token = jwtUtil.generateAccessToken(userId, studentId, role);

        // When
        Claims claims = jwtUtil.parseToken(token);

        // Then
        assertNotNull(claims);
        assertEquals("access", claims.get("type"));
        assertNotNull(claims.get("userId"));
        assertEquals(studentId, claims.get("studentId"));
        assertNotNull(claims.get("role"));
    }

    @Test
    void testGetUserIdFromToken() {
        // Given
        Long userId = 123L;
        String studentId = "2021001";
        Integer role = 0;
        String token = jwtUtil.generateAccessToken(userId, studentId, role);

        // When
        Long extractedUserId = jwtUtil.getUserIdFromToken(token);

        // Then
        assertEquals(userId, extractedUserId);
    }

    @Test
    void testGetRoleFromToken() {
        // Given
        Long userId = 1L;
        String studentId = "2021001";
        Integer role = 1; // 管理员
        String token = jwtUtil.generateAccessToken(userId, studentId, role);

        // When
        Integer extractedRole = jwtUtil.getRoleFromToken(token);

        // Then
        assertEquals(role, extractedRole);
    }

    @Test
    void testValidateToken_ValidToken() {
        // Given
        Long userId = 1L;
        String studentId = "2021001";
        Integer role = 0;
        String token = jwtUtil.generateAccessToken(userId, studentId, role);

        // When
        boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testIsTokenExpired_NotExpired() {
        // Given
        Long userId = 1L;
        String studentId = "2021001";
        Integer role = 0;
        String token = jwtUtil.generateAccessToken(userId, studentId, role);

        // When
        boolean isExpired = jwtUtil.isTokenExpired(token);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void testIsTokenExpired_ExpiredToken() {
        // Given - 创建一个已过期的令牌（过期时间设置为1毫秒）
        JwtUtil shortExpiryJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortExpiryJwtUtil, "secretKey", 
            "your-secret-key-change-this-in-production-at-least-256-bits-long");
        ReflectionTestUtils.setField(shortExpiryJwtUtil, "accessTokenExpiration", 1L);
        ReflectionTestUtils.setField(shortExpiryJwtUtil, "refreshTokenExpiration", 1L);
        
        String token = shortExpiryJwtUtil.generateAccessToken(1L, "2021001", 0);
        
        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        boolean isExpired = jwtUtil.isTokenExpired(token);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void testAccessTokenAndRefreshTokenAreDifferent() {
        // Given
        Long userId = 1L;

        // When
        String accessToken = jwtUtil.generateAccessToken(userId, "2021001", 0);
        String refreshToken = jwtUtil.generateRefreshToken(userId);

        // Then
        assertNotEquals(accessToken, refreshToken);
        
        Claims accessClaims = jwtUtil.parseToken(accessToken);
        Claims refreshClaims = jwtUtil.parseToken(refreshToken);
        
        assertEquals("access", accessClaims.get("type"));
        assertEquals("refresh", refreshClaims.get("type"));
    }
}
