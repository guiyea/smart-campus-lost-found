package com.campus.lostandfound.websocket;

import com.campus.lostandfound.util.JwtUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket消息处理器
 * 用于实时推送消息给用户
 * 
 * Requirements: 6.5 - 用户启用推送通知时，平台通过WebSocket实时推送新消息
 */
@Component
@ServerEndpoint(value = "/ws/messages/{token}", configurator = WebSocketConfigurator.class)
public class MessageWebSocket {

    private static final Logger logger = LoggerFactory.getLogger(MessageWebSocket.class);

    /**
     * 用户ID到Session的映射
     * 使用ConcurrentHashMap保证线程安全
     */
    private static final ConcurrentHashMap<Long, Session> USER_SESSIONS = new ConcurrentHashMap<>();

    /**
     * JwtUtil实例，通过WebSocketConfigurator注入
     */
    private static JwtUtil jwtUtil;

    /**
     * 设置JwtUtil实例（由WebSocketConfigurator调用）
     * 
     * @param jwtUtil JWT工具类实例
     */
    public static void setJwtUtil(JwtUtil jwtUtil) {
        MessageWebSocket.jwtUtil = jwtUtil;
    }

    /**
     * WebSocket连接建立时调用
     * 验证token，提取userId，保存Session映射
     * 
     * @param session WebSocket会话
     * @param token   JWT令牌
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        try {
            // 验证token有效性
            if (jwtUtil == null) {
                logger.error("JwtUtil not initialized");
                closeSession(session, "服务器内部错误");
                return;
            }

            if (token == null || token.isEmpty()) {
                logger.warn("WebSocket connection attempt with empty token");
                closeSession(session, "令牌不能为空");
                return;
            }

            if (!jwtUtil.validateToken(token)) {
                logger.warn("WebSocket connection attempt with invalid token");
                closeSession(session, "无效的令牌");
                return;
            }

            // 从token提取userId
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                logger.warn("WebSocket connection attempt: unable to extract userId from token");
                closeSession(session, "无法识别用户");
                return;
            }

            // 如果用户已有连接，关闭旧连接
            Session existingSession = USER_SESSIONS.get(userId);
            if (existingSession != null && existingSession.isOpen()) {
                logger.info("Closing existing WebSocket session for user: {}", userId);
                closeSession(existingSession, "新连接已建立，旧连接已关闭");
            }

            // 保存Session映射
            USER_SESSIONS.put(userId, session);
            
            // 将userId存储到session的用户属性中，便于后续使用
            session.getUserProperties().put("userId", userId);

            logger.info("WebSocket connection established for user: {}, total connections: {}", 
                    userId, USER_SESSIONS.size());

        } catch (Exception e) {
            logger.error("Error during WebSocket connection establishment", e);
            closeSession(session, "连接建立失败");
        }
    }

    /**
     * WebSocket连接关闭时调用
     * 移除Session映射
     * 
     * @param session WebSocket会话
     */
    @OnClose
    public void onClose(Session session) {
        try {
            Long userId = (Long) session.getUserProperties().get("userId");
            if (userId != null) {
                // 只有当前session是该用户的活跃session时才移除
                Session currentSession = USER_SESSIONS.get(userId);
                if (currentSession != null && currentSession.getId().equals(session.getId())) {
                    USER_SESSIONS.remove(userId);
                    logger.info("WebSocket connection closed for user: {}, remaining connections: {}", 
                            userId, USER_SESSIONS.size());
                }
            } else {
                logger.debug("WebSocket connection closed for unknown user");
            }
        } catch (Exception e) {
            logger.error("Error during WebSocket connection close", e);
        }
    }

    /**
     * WebSocket发生错误时调用
     * 记录错误日志
     * 
     * @param session   WebSocket会话
     * @param throwable 异常信息
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        Long userId = null;
        try {
            userId = (Long) session.getUserProperties().get("userId");
        } catch (Exception ignored) {
            // 忽略获取userId时的异常
        }
        
        logger.error("WebSocket error for user: {}, error: {}", 
                userId != null ? userId : "unknown", 
                throwable.getMessage(), 
                throwable);
    }

    /**
     * 接收客户端消息（可选实现）
     * 当前主要用于服务端推送，客户端消息可用于心跳检测
     * 
     * @param session WebSocket会话
     * @param message 客户端发送的消息
     */
    @OnMessage
    public void onMessage(Session session, String message) {
        Long userId = (Long) session.getUserProperties().get("userId");
        logger.debug("Received message from user {}: {}", userId, message);
        
        // 处理心跳消息
        if ("ping".equalsIgnoreCase(message)) {
            try {
                session.getBasicRemote().sendText("pong");
            } catch (IOException e) {
                logger.error("Error sending pong response to user: {}", userId, e);
            }
        }
    }

    /**
     * 向指定用户推送消息
     * 
     * @param userId  目标用户ID
     * @param message 消息内容
     * @return true表示发送成功，false表示发送失败或用户不在线
     */
    public static boolean sendToUser(Long userId, String message) {
        if (userId == null || message == null) {
            logger.warn("Cannot send message: userId or message is null");
            return false;
        }

        Session session = USER_SESSIONS.get(userId);
        if (session == null) {
            logger.debug("User {} is not connected via WebSocket", userId);
            return false;
        }

        if (!session.isOpen()) {
            logger.debug("WebSocket session for user {} is not open, removing from map", userId);
            USER_SESSIONS.remove(userId);
            return false;
        }

        try {
            session.getBasicRemote().sendText(message);
            logger.debug("Message sent to user {}: {}", userId, message);
            return true;
        } catch (IOException e) {
            logger.error("Error sending message to user: {}", userId, e);
            return false;
        }
    }

    /**
     * 检查用户是否在线（有活跃的WebSocket连接）
     * 
     * @param userId 用户ID
     * @return true表示在线，false表示不在线
     */
    public static boolean isUserOnline(Long userId) {
        if (userId == null) {
            return false;
        }
        Session session = USER_SESSIONS.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 获取当前在线用户数量
     * 
     * @return 在线用户数量
     */
    public static int getOnlineUserCount() {
        return USER_SESSIONS.size();
    }

    /**
     * 关闭WebSocket会话并发送关闭原因
     * 
     * @param session WebSocket会话
     * @param reason  关闭原因
     */
    private void closeSession(Session session, String reason) {
        try {
            if (session != null && session.isOpen()) {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, reason));
            }
        } catch (IOException e) {
            logger.error("Error closing WebSocket session: {}", e.getMessage());
        }
    }
}
