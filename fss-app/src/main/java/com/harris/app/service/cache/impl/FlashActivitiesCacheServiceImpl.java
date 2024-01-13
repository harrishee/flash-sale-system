package com.harris.app.service.cache.impl;

import com.harris.app.model.cache.FlashActivitiesCache;
import com.harris.app.service.cache.FlashActivitiesCacheService;

public class FlashActivitiesCacheServiceImpl implements FlashActivitiesCacheService {
    @Override
    public FlashActivitiesCache getActivitiesCache(Integer pageNumber, Long version) {
        return null;
    }

    @Override
    public FlashActivitiesCache tryUpdateActivitiesCacheByLock(Integer pageNumber) {
        return null;
    }
}
