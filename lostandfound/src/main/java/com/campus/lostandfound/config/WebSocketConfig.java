package com.campus.lostandfound.config;

import com.campus.lostandfound.websocket.WebSocketConfigurator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类
 * 配置WebSocket端点和CORS策略
 * 
 * Requirements: 6.5 - 用户启用推送通知时，平台通过WebSocket实时推送新消息
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    /**
     * 注册WebSocket处理器
     * 配置WebSocket端点路径和允许的来源
     * 
     * @param registry WebSocket处理器注册器
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // WebSocket端点通过@ServerEndpoint注解在MessageWebSocket中配置
        // 路径: /ws/messages/{token}
        // 允许所有来源（CORS配置）
    }

    /**
     * 配置ServerEndpointExporter
     * 用于支持@ServerEndpoint注解的WebSocket端点
     * 
     * @return ServerEndpointExporter Bean
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    /**
     * 配置WebSocketConfigurator
     * 用于在WebSocket端点中注入Spring Bean
     * 
     * @return WebSocketConfigurator Bean
     */
    @Bean
    public WebSocketConfigurator webSocketConfigurator() {
        return new WebSocketConfigurator();
    }
}
