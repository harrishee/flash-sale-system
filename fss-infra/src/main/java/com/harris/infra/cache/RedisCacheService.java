package com.harris.infra.cache;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class RedisCacheService implements DistributedCacheService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void put(String key, String value) {
        if (StringUtils.isEmpty(key) || value == null) {
            return;
        }
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void put(String key, Object value) {
        if (StringUtils.isEmpty(key) || value == null) {
            return;
        }
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void put(String key, Object value, long timeout) {
        if (StringUtils.isEmpty(key) || value == null) {
            return;
        }
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    @Override
    public void put(String key, Object value, long timeout, TimeUnit unit) {
        if (StringUtils.isEmpty(key) || value == null) {
            return;
        }
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    @Override
    public <T> T getObject(String key, Class<T> targetClass) {
        // Retrieve the object from Redis and validate
        Object res = redisTemplate.opsForValue().get(key);
        if (res == null) {
            return null;
        }

        // Parse the object (expected to be a String in JSON format) to the specified class type
        try {
            return JSON.parseObject((String) res, targetClass);
        } catch (Exception e) {
            return null;
        }
    }
}
