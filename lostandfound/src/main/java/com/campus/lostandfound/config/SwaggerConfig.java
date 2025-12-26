package com.campus.lostandfound.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 配置类
 * 配置 API 文档信息、安全方案和 API 分组
 */
@Configuration
public class SwaggerConfig {

    /**
     * 配置 OpenAPI 基本信息和安全方案
     * 包括标题、描述、版本、联系信息以及 JWT Bearer 认证方案
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("校园失物招领系统 API")
                        .description("提供失物发布、搜索、匹配、消息通知等功能的 RESTful API")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("dev@campus.edu"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("输入 JWT Token，格式：Bearer {token}")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    /**
     * 用户端 API 分组
     * 包含认证、用户、物品、匹配、消息、积分、文件上传等接口
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("user-api")
                .displayName("用户端 API")
                .pathsToMatch(
                        "/api/v1/auth/**",
                        "/api/v1/users/**",
                        "/api/v1/items/**",
                        "/api/v1/matches/**",
                        "/api/v1/messages/**",
                        "/api/v1/points/**",
                        "/api/v1/files/**"
                )
                .build();
    }

    /**
     * 管理端 API 分组
     * 包含统计、审核、用户管理等管理员专用接口
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin-api")
                .displayName("管理端 API")
                .pathsToMatch("/api/v1/admin/**")
                .build();
    }
}

