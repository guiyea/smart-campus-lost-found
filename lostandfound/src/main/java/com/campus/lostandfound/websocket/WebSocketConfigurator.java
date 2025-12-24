package com.campus.lostandfound.websocket;

import com.campus.lostandfound.util.JwtUtil;
import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * WebSocket配置器
 * 用于在WebSocket端点中注入Spring Bean
 * 
 * 由于@ServerEndpoint是Jakarta EE注解，不直接支持Spring依赖注入，
 * 需要通过此配置器来获取Spring ApplicationContext中的Bean
 */
@Component
public class WebSocketConfigurator extends ServerEndpointConfig.Configurator implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * 设置Spring ApplicationContext
     * 
     * @param applicationContext Spring应用上下文
     * @throws BeansException Bean异常
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        WebSocketConfigurator.applicationContext = applicationContext;
        
        // 初始化MessageWebSocket中的JwtUtil
        if (applicationContext != null) {
            try {
                JwtUtil jwtUtil = applicationContext.getBean(JwtUtil.class);
                MessageWebSocket.setJwtUtil(jwtUtil);
            } catch (BeansException e) {
                // JwtUtil可能还未初始化，稍后会在getEndpointInstance中再次尝试
            }
        }
    }

    /**
     * 获取WebSocket端点实例
     * 重写此方法以支持Spring Bean注入
     * 
     * @param clazz 端点类
     * @param <T>   端点类型
     * @return 端点实例
     * @throws InstantiationException 实例化异常
     */
    @Override
    public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException {
        // 确保JwtUtil已注入
        if (applicationContext != null) {
            try {
                JwtUtil jwtUtil = applicationContext.getBean(JwtUtil.class);
                MessageWebSocket.setJwtUtil(jwtUtil);
            } catch (BeansException e) {
                throw new InstantiationException("Failed to get JwtUtil bean: " + e.getMessage());
            }
        }
        
        // 尝试从Spring容器获取实例
        if (applicationContext != null && applicationContext.containsBean(clazz.getSimpleName().substring(0, 1).toLowerCase() + clazz.getSimpleName().substring(1))) {
            return applicationContext.getBean(clazz);
        }
        
        // 如果Spring容器中没有，则创建新实例
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new InstantiationException("Failed to create endpoint instance: " + e.getMessage());
        }
    }

    /**
     * 获取Spring ApplicationContext
     * 
     * @return ApplicationContext实例
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
