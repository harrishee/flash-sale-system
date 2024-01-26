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
        // Current time in milliseconds
        long current = System.currentTimeMillis();
        // Calculate the length of the window in milliseconds
        long length = (long) period * size;
        // Calculate the start of the window
        long start = current - length;
        // Calculate expiration time for the Redis key
        long expireTime = length + period;

        // Add the current timestamp to the Redis sorted set
        redisTemplate.opsForZSet().add(userActionKey, String.valueOf(current), current);
        // Remove all elements in the Redis sorted set with a score between [0, start]
        redisTemplate.opsForZSet().removeRangeByScore(userActionKey, 0, start);
        // Get the number of elements in the Redis sorted set
        Long count = redisTemplate.opsForZSet().zCard(userActionKey);
        // Set the expiration time for the Redis key
        redisTemplate.expire(userActionKey, expireTime, TimeUnit.MILLISECONDS);

        // Return false if count is null or if the count exceeds the size limit.
        return count != null && count <= size;
    }
}
