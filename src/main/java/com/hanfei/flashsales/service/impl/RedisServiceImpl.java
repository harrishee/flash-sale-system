package com.hanfei.flashsales.service.impl;

import com.hanfei.flashsales.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;

import java.util.Collections;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Slf4j
@Controller
public class RedisServiceImpl implements RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisScript<Long> redisScript;

    @Override
    public boolean stockDeductValidator(Long activityId) {
        String key = "activity:" + activityId;
        // Lua script: check stock, deduct stock, and return the stock after deduction
        Long stock = redisTemplate.execute(redisScript, Collections.singletonList(key), Collections.EMPTY_LIST);
        return stock >= 0;
    }

    @Override
    public void incrementValueByKey(String key) {
        redisTemplate.opsForValue().increment(key);
    }

    @Override
    public void addLimitMember(Long activityId, Long userId) {
        redisTemplate.opsForSet().add("activity_limited_users:" + activityId, userId);
    }

    @Override
    public boolean isInLimitMember(Long activityId, Long userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("activity_limited_users:" + activityId, userId));
    }

    @Override
    public void removeLimitMember(Long activityId, Long userId) {
        redisTemplate.opsForSet().remove("activity_limited_users:" + activityId, userId);
    }
}
