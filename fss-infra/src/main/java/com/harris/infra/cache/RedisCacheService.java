package com.harris.infra.cache;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Getter
@Component
public class RedisCacheService implements DistributedCacheService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public void put(String key, String value) {
        if (StringUtils.isEmpty(key) || value == null) return;
        redisTemplate.opsForValue().set(key, value);
    }
    
    @Override
    public void put(String key, Object value) {
        if (StringUtils.isEmpty(key) || value == null) return;
        redisTemplate.opsForValue().set(key, value);
    }
    
    @Override
    public void put(String key, Object value, long timeout) {
        if (StringUtils.isEmpty(key) || value == null) return;
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }
    
    @Override
    public void put(String key, Object value, long timeout, TimeUnit unit) {
        if (StringUtils.isEmpty(key) || value == null) return;
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }
    
    @Override
    public <T> T getObject(String key, Class<T> targetClass) {
        // 用 redisTemplate 获取 key 对应的 object 类型的 value
        Object res = redisTemplate.opsForValue().get(key);
        if (res == null) return null;
        
        // 直接返回目标类型匹配的对象
        if (targetClass.isInstance(res)) return targetClass.cast(res);
        
        // 如果结果是字符串，尝试解析为目标类
        if (res instanceof String) {
            try {
                return JSON.parseObject((String) res, targetClass);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        
        return null;
    }
}
