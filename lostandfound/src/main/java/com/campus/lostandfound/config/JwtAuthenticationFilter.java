package com.campus.lostandfound.config;

import com.campus.lostandfound.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 * 继承OncePerRequestFilter确保每个请求只执行一次
 * 负责从请求头中提取JWT令牌，验证令牌有效性，并设置认证信息到SecurityContext
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 执行过滤逻辑
     * 1. 从请求头Authorization提取Bearer令牌
     * 2. 验证令牌有效性
     * 3. 提取用户信息并设置到SecurityContext
     * 4. 令牌无效或过期时不设置认证信息，让后续过滤器处理
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 从请求头中提取JWT令牌
            String token = extractTokenFromRequest(request);
            
            // 如果令牌存在且有效，设置认证信息
            if (token != null && jwtUtil.validateToken(token)) {
                // 从令牌中提取用户信息
                Long userId = jwtUtil.getUserIdFromToken(token);
                Integer role = jwtUtil.getRoleFromToken(token);
                
                // 创建认证对象
                // 使用userId作为principal，role作为权限
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userId,  // principal: 用户ID
                        null,    // credentials: 不需要密码
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + (role == 1 ? "ADMIN" : "USER")))
                    );
                
                // 设置请求详情
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 将认证信息设置到SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            
        } catch (Exception e) {
            // 令牌解析失败或其他异常，不设置认证信息
            // 让后续的过滤器和异常处理器处理
            logger.debug("JWT authentication failed: " + e.getMessage());
        }
        
        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取JWT令牌
     * 
     * @param request HTTP请求
     * @return JWT令牌，如果不存在或格式不正确则返回null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        // 检查Authorization头是否存在且以"Bearer "开头
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // 提取"Bearer "之后的令牌部分
            return bearerToken.substring(7);
        }
        
        return null;
    }
}
