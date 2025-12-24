package com.campus.lostandfound.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis缓存配置类
 * 启用Spring Cache并配置Redis作为缓存存储
 * 
 * 缓存策略:
 * - 物品详情缓存: 30分钟过期
 * - 物品搜索缓存: 5分钟过期
 * - 用户信息缓存: 1小时过期
 * - 积分排行榜缓存: 10分钟过期
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {
    
    // 缓存名称常量
    public static final String CACHE_ITEM_DETAIL = "itemDetail";
    public static final String CACHE_ITEM_SEARCH = "itemSearch";
    public static final String CACHE_USER_INFO = "userInfo";
    public static final String CACHE_POINT_RANKING = "pointRanking";
    
    /**
     * 配置CacheManager
     * 使用Redis作为缓存存储，支持不同缓存的不同过期时间
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 创建ObjectMapper用于JSON序列化
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 创建JSON序列化器
        GenericJackson2JsonRedisSerializer jsonSerializer = 
                new GenericJackson2JsonRedisSerializer(objectMapper);
        
        // 默认缓存配置（30分钟过期）
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();
        
        // 不同缓存的配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 物品详情缓存: 30分钟
        cacheConfigurations.put(CACHE_ITEM_DETAIL, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 物品搜索缓存: 5分钟
        cacheConfigurations.put(CACHE_ITEM_SEARCH, defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // 用户信息缓存: 1小时
        cacheConfigurations.put(CACHE_USER_INFO, defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // 积分排行榜缓存: 10分钟
        cacheConfigurations.put(CACHE_POINT_RANKING, defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
