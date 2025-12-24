package com.campus.lostandfound.config;

import com.campus.lostandfound.exception.RateLimitException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 限流拦截器
 * 使用Redis滑动窗口算法实现接口限流
 * 限制单IP每分钟最多100次请求
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    /**
     * 限流key前缀
     */
    private static final String RATE_LIMIT_KEY_PREFIX = "rate:limit:";

    /**
     * 时间窗口大小（毫秒）- 1分钟
     */
    private static final long WINDOW_SIZE_MS = 60 * 1000;

    /**
     * 最大请求次数
     */
    private static final int MAX_REQUESTS = 100;

    /**
     * key过期时间（秒）- 2分钟，确保过期数据被清理
     */
    private static final long KEY_EXPIRE_SECONDS = 120;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * IP白名单，从配置文件读取，多个IP用逗号分隔
     */
    @Value("${rate-limit.whitelist-ips:127.0.0.1,0:0:0:0:0:0:0:1}")
    private String whitelistIps;

    /**
     * 是否启用限流
     */
    @Value("${rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果限流未启用，直接放行
        if (!rateLimitEnabled) {
            return true;
        }

        String clientIp = getClientIp(request);
        
        // 检查IP是否在白名单中
        if (isWhitelisted(clientIp)) {
            logger.debug("IP {} 在白名单中，跳过限流检查", clientIp);
            return true;
        }

        String key = RATE_LIMIT_KEY_PREFIX + clientIp;
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - WINDOW_SIZE_MS;

        try {
            // 使用ZSET实现滑动窗口
            // 1. 移除1分钟前的记录
            stringRedisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

            // 2. 统计当前窗口内的请求数量
            Long count = stringRedisTemplate.opsForZSet().zCard(key);
            
            if (count != null && count >= MAX_REQUESTS) {
                logger.warn("IP {} 请求频率超限，当前请求数: {}", clientIp, count);
                throw new RateLimitException("请求过于频繁，请稍后再试");
            }

            // 3. 添加当前请求的时间戳
            // 使用时间戳作为score和value，确保唯一性
            String member = String.valueOf(currentTime) + ":" + System.nanoTime();
            stringRedisTemplate.opsForZSet().add(key, member, currentTime);

            // 4. 设置key的过期时间，防止内存泄漏
            stringRedisTemplate.expire(key, KEY_EXPIRE_SECONDS, TimeUnit.SECONDS);

            return true;
        } catch (RateLimitException e) {
            throw e;
        } catch (Exception e) {
            // Redis异常时，记录日志但不阻止请求（降级处理）
            logger.error("限流检查异常，IP: {}, 错误: {}", clientIp, e.getMessage());
            return true;
        }
    }

    /**
     * 获取客户端真实IP地址
     * 支持代理服务器场景
     *
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }

    /**
     * 检查IP是否在白名单中
     *
     * @param ip 客户端IP
     * @return 是否在白名单中
     */
    private boolean isWhitelisted(String ip) {
        if (ip == null || whitelistIps == null || whitelistIps.isEmpty()) {
            return false;
        }
        List<String> whitelist = Arrays.asList(whitelistIps.split(","));
        return whitelist.stream()
                .map(String::trim)
                .anyMatch(whitelistedIp -> whitelistedIp.equals(ip));
    }
}
