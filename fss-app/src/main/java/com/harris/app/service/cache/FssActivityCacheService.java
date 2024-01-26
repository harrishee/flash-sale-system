package com.harris.app.service.cache;

import com.harris.app.model.cache.SaleActivityCache;

public interface FssActivityCacheService {
    SaleActivityCache getActivityCache(Long activityId, Long version);

    SaleActivityCache tryUpdateActivityCacheByLock(Long activityId);
}
