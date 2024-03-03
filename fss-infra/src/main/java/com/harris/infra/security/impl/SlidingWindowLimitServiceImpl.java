package com.harris.infra.security.impl;

import com.harris.infra.security.SlidingWindowLimitService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class SlidingWindowLimitServiceImpl implements SlidingWindowLimitService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public boolean pass(String userActionKey, int period, int size) {
        long current = System.currentTimeMillis(); // 当前时间（毫秒）
        long length = (long) period * size; // 计算窗口长度（毫秒）
        long start = current - length; // 计算窗口开始时间
        long expireTime = length + period; // 计算Redis键的过期时间
        
        // 将当前时间戳添加到Redis有序集合
        redisTemplate.opsForZSet().add(userActionKey, String.valueOf(current), current);
        // 移除有序集合中分数在[0, start]之间的所有元素
        redisTemplate.opsForZSet().removeRangeByScore(userActionKey, 0, start);
        // 获取有序集合中的元素数量
        Long count = redisTemplate.opsForZSet().zCard(userActionKey);
        // 设置Redis键的过期时间
        redisTemplate.expire(userActionKey, expireTime, TimeUnit.MILLISECONDS);
        
        // 如果count为null或者元素数量超过了大小限制，则返回false
        return count != null && count <= size;
    }
}
