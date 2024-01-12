package com.harris.infra.cache;

import com.alibaba.fastjson.JSON;
import com.harris.infra.cache.DistributedCacheService;
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
    public void put(String key, Object value, long expireTime) {
        if (StringUtils.isEmpty(key) || value == null) {
            return;
        }
        redisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.SECONDS);
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
        Object res = redisTemplate.opsForValue().get(key);
        if (res == null) {
            return null;
        }
        try {
            return JSON.parseObject((String) res, targetClass);
        } catch (Exception e) {
            return null;
        }
    }
}
