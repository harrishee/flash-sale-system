package com.harris.app.service.cache;

import com.harris.app.model.cache.FlashItemsCache;

public interface FlashItemsCacheService {
    FlashItemsCache getItemsCache(Long activityId, Long version);
    FlashItemsCache tryUpdateItemsCacheByLock(Long activityId);
}
