package com.campus.lostandfound;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

/**
 * 简单的 Redis 连接测试
 * 直接运行 main 方法测试 Redis 连接
 */
public class SimpleRedisTest {
    
    public static void main(String[] args) {
        System.out.println("=== Redis 连接测试 ===\n");
        
        String host = "localhost";
        int port = 6379;
        
        System.out.println("尝试连接到 Redis...");
        System.out.println("主机: " + host);
        System.out.println("端口: " + port);
        System.out.println();
        
        RedisClient redisClient = null;
        StatefulRedisConnection<String, String> connection = null;
        
        try {
            // 创建 Redis URI
            RedisURI redisUri = RedisURI.Builder
                    .redis(host, port)
                    .build();
            
            // 创建 Redis 客户端
            redisClient = RedisClient.create(redisUri);
            connection = redisClient.connect();
            RedisCommands<String, String> commands = connection.sync();
            
            // 测试 PING 命令
            System.out.println("1. 测试 PING 命令...");
            String pong = commands.ping();
            System.out.println("   响应: " + pong);
            System.out.println("   ✓ PING 测试成功!\n");
            
            // 测试 SET 命令
            System.out.println("2. 测试 SET 命令...");
            String key = "test:connection";
            String value = "Redis连接成功!";
            commands.set(key, value);
            System.out.println("   写入键: " + key);
            System.out.println("   写入值: " + value);
            System.out.println("   ✓ SET 测试成功!\n");
            
            // 测试 GET 命令
            System.out.println("3. 测试 GET 命令...");
            String result = commands.get(key);
            System.out.println("   读取键: " + key);
            System.out.println("   读取值: " + result);
            System.out.println("   ✓ GET 测试成功!\n");
            
            // 测试 DEL 命令
            System.out.println("4. 测试 DEL 命令...");
            commands.del(key);
            System.out.println("   删除键: " + key);
            System.out.println("   ✓ DEL 测试成功!\n");
            
            // 测试 INFO 命令
            System.out.println("5. 获取 Redis 服务器信息...");
            String info = commands.info("server");
            String[] lines = info.split("\r\n");
            for (String line : lines) {
                if (line.startsWith("redis_version:") || 
                    line.startsWith("redis_mode:") || 
                    line.startsWith("os:") ||
                    line.startsWith("uptime_in_days:")) {
                    System.out.println("   " + line);
                }
            }
            System.out.println("   ✓ INFO 测试成功!\n");
            
            System.out.println("===================");
            System.out.println("✓✓✓ 所有测试通过! ✓✓✓");
            System.out.println("Redis 本地连接正常!");
            System.out.println("===================");
            
        } catch (Exception e) {
            System.err.println("✗ Redis 连接失败!");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("\n可能的原因:");
            System.err.println("1. Redis 服务未启动");
            System.err.println("2. Redis 端口不是 6379");
            System.err.println("3. Redis 需要密码认证");
            System.err.println("4. 防火墙阻止了连接");
            System.err.println("\n请检查:");
            System.err.println("- Windows: 在服务中查看 Redis 服务状态");
            System.err.println("- 或运行: redis-server.exe");
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    // 忽略关闭时的异常
                }
            }
            if (redisClient != null) {
                try {
                    redisClient.shutdown();
                    System.out.println("\nRedis 连接已关闭。");
                } catch (Exception e) {
                    // 忽略关闭时的异常
                }
            }
        }
    }
}
