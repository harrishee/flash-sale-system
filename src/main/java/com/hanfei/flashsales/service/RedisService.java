package com.hanfei.flashsales.service;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
public interface RedisService {

    boolean stockDeductValidator(Long activityId);

    void incrementValueByKey(String key);

    void addLimitMember(long activityId, String userId);

    boolean isInLimitMember(long activityId, String userId);

    void removeLimitMember(Long activityId, String userId);
}
