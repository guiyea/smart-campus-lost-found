package com.campus.lostandfound.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 * 提供常用的Redis操作方法
 */
@Component
public class RedisUtil {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 设置键值对，带过期时间
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取键对应的值
     *
     * @param key 键
     * @return 值，如果键不存在则返回null
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 删除键
     *
     * @param key 键
     * @return true表示删除成功，false表示键不存在
     */
    public Boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }

    /**
     * 批量删除键
     *
     * @param keys 键集合
     * @return 删除的键数量
     */
    public Long delete(Collection<String> keys) {
        return stringRedisTemplate.delete(keys);
    }

    /**
     * 递增键的值
     *
     * @param key 键
     * @return 递增后的值
     */
    public Long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

    /**
     * 设置键的过期时间
     *
     * @param key     键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return true表示设置成功，false表示设置失败
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return stringRedisTemplate.expire(key, timeout, unit);
    }

    /**
     * 检查键是否存在
     *
     * @param key 键
     * @return true表示存在，false表示不存在
     */
    public Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    // ==================== ZSET操作 ====================

    /**
     * 向有序集合添加元素
     *
     * @param key   键
     * @param value 值
     * @param score 分数
     * @return true表示添加成功
     */
    public Boolean zAdd(String key, String value, double score) {
        return stringRedisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 批量向有序集合添加元素
     *
     * @param key    键
     * @param tuples 元素集合
     * @return 添加的元素数量
     */
    public Long zAdd(String key, Set<ZSetOperations.TypedTuple<String>> tuples) {
        return stringRedisTemplate.opsForZSet().add(key, tuples);
    }

    /**
     * 获取有序集合指定范围的元素（按分数从高到低）
     *
     * @param key   键
     * @param start 开始位置
     * @param end   结束位置
     * @return 元素集合
     */
    public Set<String> zRevRange(String key, long start, long end) {
        return stringRedisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * 获取有序集合指定范围的元素及分数（按分数从高到低）
     *
     * @param key   键
     * @param start 开始位置
     * @param end   结束位置
     * @return 元素及分数集合
     */
    public Set<ZSetOperations.TypedTuple<String>> zRevRangeWithScores(String key, long start, long end) {
        return stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }

    /**
     * 获取有序集合的大小
     *
     * @param key 键
     * @return 集合大小
     */
    public Long zCard(String key) {
        return stringRedisTemplate.opsForZSet().zCard(key);
    }

    /**
     * 删除有序集合中的元素
     *
     * @param key    键
     * @param values 要删除的元素
     * @return 删除的元素数量
     */
    public Long zRemove(String key, Object... values) {
        return stringRedisTemplate.opsForZSet().remove(key, values);
    }

    /**
     * 获取元素在有序集合中的分数
     *
     * @param key   键
     * @param value 元素
     * @return 分数
     */
    public Double zScore(String key, String value) {
        return stringRedisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 增加有序集合中元素的分数
     *
     * @param key   键
     * @param value 元素
     * @param delta 增加的分数
     * @return 增加后的分数
     */
    public Double zIncrementScore(String key, String value, double delta) {
        return stringRedisTemplate.opsForZSet().incrementScore(key, value, delta);
    }

    // ==================== 模式匹配操作 ====================

    /**
     * 根据模式获取所有匹配的键
     *
     * @param pattern 模式（如 "item:detail:*"）
     * @return 匹配的键集合
     */
    public Set<String> keys(String pattern) {
        return stringRedisTemplate.keys(pattern);
    }
}
