package com.campus.lostandfound;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis连接测试
 * 使用 @DataRedisTest 只加载 Redis 相关配置,避免加载 WebSocket 等其他配置
 */
@DataRedisTest
@TestPropertySource(properties = {
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379"
})
public class RedisConnectionTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedisConnection() {
        // 测试基本的set和get操作
        String testKey = "test:connection";
        String testValue = "Redis连接成功!";
        
        // 写入数据
        stringRedisTemplate.opsForValue().set(testKey, testValue);
        
        // 读取数据
        String result = stringRedisTemplate.opsForValue().get(testKey);
        
        // 验证
        assertNotNull(result, "Redis读取结果不应为null");
        assertEquals(testValue, result, "Redis读取的值应该与写入的值相同");
        
        // 清理测试数据
        stringRedisTemplate.delete(testKey);
        
        System.out.println("✓ Redis连接测试成功!");
        System.out.println("  写入值: " + testValue);
        System.out.println("  读取值: " + result);
    }

    @Test
    public void testRedisOperations() {
        String key = "test:operations";
        
        try {
            // 测试字符串操作
            stringRedisTemplate.opsForValue().set(key, "value1");
            assertEquals("value1", stringRedisTemplate.opsForValue().get(key));
            
            // 测试递增操作
            String counterKey = "test:counter";
            stringRedisTemplate.opsForValue().set(counterKey, "0");
            stringRedisTemplate.opsForValue().increment(counterKey);
            assertEquals("1", stringRedisTemplate.opsForValue().get(counterKey));
            
            // 测试过期时间
            String expireKey = "test:expire";
            stringRedisTemplate.opsForValue().set(expireKey, "expire-value");
            stringRedisTemplate.expire(expireKey, 10, java.util.concurrent.TimeUnit.SECONDS);
            Long ttl = stringRedisTemplate.getExpire(expireKey);
            assertTrue(ttl > 0 && ttl <= 10, "TTL应该在0-10秒之间");
            
            System.out.println("✓ Redis操作测试成功!");
            System.out.println("  字符串操作: 通过");
            System.out.println("  递增操作: 通过");
            System.out.println("  过期时间设置: 通过 (TTL=" + ttl + "秒)");
            
        } finally {
            // 清理测试数据
            stringRedisTemplate.delete(key);
            stringRedisTemplate.delete("test:counter");
            stringRedisTemplate.delete("test:expire");
        }
    }

    @Test
    public void testRedisPing() {
        try {
            // 执行PING命令测试连接
            String pong = stringRedisTemplate.execute(
                (org.springframework.data.redis.core.RedisCallback<String>) connection -> {
                    return connection.ping();
                }
            );
            
            assertNotNull(pong, "PING命令应该返回结果");
            System.out.println("✓ Redis PING测试成功!");
            System.out.println("  响应: " + pong);
            
        } catch (Exception e) {
            fail("Redis连接失败: " + e.getMessage());
        }
    }
}
