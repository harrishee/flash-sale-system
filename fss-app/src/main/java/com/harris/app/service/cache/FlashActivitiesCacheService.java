package com.harris.app.service.cache;

import com.harris.app.model.cache.FlashActivitiesCache;

public interface FlashActivitiesCacheService {
    FlashActivitiesCache getActivitiesCache(Integer pageNumber, Long version);

    FlashActivitiesCache tryUpdateActivitiesCacheByLock(Integer pageNumber);
}
