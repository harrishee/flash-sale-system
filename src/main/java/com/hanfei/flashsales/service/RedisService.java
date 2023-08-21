package com.hanfei.flashsales.service;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
public interface RedisService {

    boolean stockDeductValidator(Long activityId);

    void incrementValueByKey(String key);

    void addLimitMember(Long activityId, Long userId);

    boolean isInLimitMember(Long activityId, Long userId);

    void removeLimitMember(Long activityId, Long userId);
}
