package com.harris.app.service.cache;

import com.harris.app.model.cache.FlashActivityCache;

public interface FlashActivityCacheService {
    FlashActivityCache getActivityCache(Long activityId, Long version);

    FlashActivityCache tryUpdateActivityCacheByLock(Long activityId);
}
