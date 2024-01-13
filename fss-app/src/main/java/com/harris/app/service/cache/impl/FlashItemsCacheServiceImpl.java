package com.harris.app.service.cache.impl;

import com.harris.app.model.cache.FlashItemsCache;
import com.harris.app.service.cache.FlashItemsCacheService;

public class FlashItemsCacheServiceImpl implements FlashItemsCacheService {
    @Override
    public FlashItemsCache getItemsCache(Long activityId, Long version) {
        return null;
    }

    @Override
    public FlashItemsCache tryUpdateItemsCacheByLock(Long activityId) {
        return null;
    }
}
