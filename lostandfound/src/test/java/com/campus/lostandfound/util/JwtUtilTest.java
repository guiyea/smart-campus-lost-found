package com.campus.lostandfound.util;

import io.jsonwebtoken.Claims;
import net.jqwik.api.*;
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

    // Property-based tests using jqwik
    
    /**
     * Property 2: 登录认证往返一致性
     * Feature: smart-campus-lost-found, Property 2: 登录认证往返一致性
     * Validates: Requirements 1.2, 10.1
     */
    @Property(tries = 100)
    void testTokenRoundTripConsistency(@ForAll("validUserIds") Long userId, 
                                     @ForAll("validRoles") Integer role) {
        // Given - 设置JwtUtil实例
        JwtUtil testJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(testJwtUtil, "secretKey", 
            "your-secret-key-change-this-in-production-at-least-256-bits-long");
        ReflectionTestUtils.setField(testJwtUtil, "accessTokenExpiration", 7200000L); // 2小时
        ReflectionTestUtils.setField(testJwtUtil, "refreshTokenExpiration", 604800000L); // 7天
        
        String studentId = "STU" + userId; // 生成对应的学号
        
        // When - 生成令牌并解析
        String token = testJwtUtil.generateAccessToken(userId, studentId, role);
        Long extractedUserId = testJwtUtil.getUserIdFromToken(token);
        Integer extractedRole = testJwtUtil.getRoleFromToken(token);
        
        // Then - 解析后应返回相同的用户ID和角色
        assertEquals(userId, extractedUserId, 
            "Token round trip should preserve user ID");
        assertEquals(role, extractedRole, 
            "Token round trip should preserve user role");
    }
    
    /**
     * Property 2: 登录认证往返一致性 - 过期令牌验证
     * Feature: smart-campus-lost-found, Property 2: 登录认证往返一致性
     * Validates: Requirements 1.2, 10.1
     */
    @Property(tries = 100)
    void testExpiredTokenValidation(@ForAll("validUserIds") Long userId,
                                  @ForAll("validRoles") Integer role) {
        // Given - 创建一个过期时间很短的JwtUtil实例
        JwtUtil expiredJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(expiredJwtUtil, "secretKey", 
            "your-secret-key-change-this-in-production-at-least-256-bits-long");
        ReflectionTestUtils.setField(expiredJwtUtil, "accessTokenExpiration", 1L); // 1毫秒
        ReflectionTestUtils.setField(expiredJwtUtil, "refreshTokenExpiration", 1L);
        
        // 创建一个正常的JwtUtil实例用于验证
        JwtUtil validationJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(validationJwtUtil, "secretKey", 
            "your-secret-key-change-this-in-production-at-least-256-bits-long");
        ReflectionTestUtils.setField(validationJwtUtil, "accessTokenExpiration", 7200000L);
        ReflectionTestUtils.setField(validationJwtUtil, "refreshTokenExpiration", 604800000L);
        
        String studentId = "STU" + userId;
        String token = expiredJwtUtil.generateAccessToken(userId, studentId, role);
        
        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When & Then - 对于任意过期令牌，validateToken应返回false
        assertFalse(validationJwtUtil.validateToken(token), 
            "Expired token should be invalid");
        assertTrue(validationJwtUtil.isTokenExpired(token), 
            "Expired token should be detected as expired");
    }
    
    // Generators for property-based tests
    
    @Provide
    Arbitrary<Long> validUserIds() {
        return Arbitraries.longs().between(1L, 999999L);
    }
    
    @Provide 
    Arbitrary<Integer> validRoles() {
        return Arbitraries.integers().between(0, 1); // 0: 普通用户, 1: 管理员
    }
}
