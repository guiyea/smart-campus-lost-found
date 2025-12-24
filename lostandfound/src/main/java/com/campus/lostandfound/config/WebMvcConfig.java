package com.campus.lostandfound.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Web MVC配置类
 * 配置拦截器、跨域等Web相关设置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    /**
     * IP白名单，从配置文件读取，多个IP用逗号分隔
     */
    @Value("${rate-limit.whitelist-ips:127.0.0.1,0:0:0:0:0:0:0:1}")
    private String whitelistIps;

    /**
     * 不需要限流的路径模式
     */
    private static final List<String> EXCLUDE_PATTERNS = Arrays.asList(
            // 静态资源
            "/static/**",
            "/css/**",
            "/js/**",
            "/images/**",
            "/fonts/**",
            "/favicon.ico",
            "/*.html",
            "/*.css",
            "/*.js",
            
            // Swagger/OpenAPI文档
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            
            // 健康检查接口
            "/actuator/**",
            "/health",
            "/api/v1/health",
            
            // WebSocket端点
            "/ws/**"
    );

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**")  // 拦截所有路径
                .excludePathPatterns(EXCLUDE_PATTERNS);  // 排除不需要限流的路径
    }

    /**
     * 获取IP白名单列表
     *
     * @return IP白名单列表
     */
    public List<String> getWhitelistIps() {
        return Arrays.asList(whitelistIps.split(","));
    }
}
