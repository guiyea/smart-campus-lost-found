package com.campus.lostandfound.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成、解析和验证JWT令牌
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    /**
     * 生成访问令牌
     *
     * @param userId    用户ID
     * @param studentId 学号
     * @param role      用户角色
     * @return JWT访问令牌
     */
    public String generateAccessToken(Long userId, String studentId, Integer role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("studentId", studentId);
        claims.put("role", role);
        claims.put("type", "access");

        return createToken(claims, accessTokenExpiration);
    }

    /**
     * 生成刷新令牌
     *
     * @param userId 用户ID
     * @return JWT刷新令牌
     */
    public String generateRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");

        return createToken(claims, refreshTokenExpiration);
    }

    /**
     * 创建令牌
     *
     * @param claims     声明信息
     * @param expiration 过期时间（毫秒）
     * @return JWT令牌
     */
    private String createToken(Map<String, Object> claims, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析令牌
     *
     * @param token JWT令牌
     * @return Claims对象
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从令牌中提取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        Object userId = claims.get("userId");
        
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            return (Long) userId;
        }
        
        return Long.parseLong(userId.toString());
    }

    /**
     * 从令牌中提取用户角色
     *
     * @param token JWT令牌
     * @return 用户角色
     */
    public Integer getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        Object role = claims.get("role");
        
        if (role instanceof Integer) {
            return (Integer) role;
        }
        
        return Integer.parseInt(role.toString());
    }

    /**
     * 验证令牌有效性
     *
     * @param token JWT令牌
     * @return true表示有效，false表示无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查令牌是否过期
     *
     * @param token JWT令牌
     * @return true表示已过期，false表示未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 获取签名密钥
     *
     * @return SecretKey对象
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
