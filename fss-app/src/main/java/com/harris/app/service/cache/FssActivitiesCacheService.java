package com.harris.app.service.cache;

import com.harris.app.model.cache.SaleActivitiesCache;

public interface FssActivitiesCacheService {
    SaleActivitiesCache getActivitiesCache(Integer pageNumber, Long version);

    SaleActivitiesCache tryUpdateActivitiesCacheByLock(Integer pageNumber);
}
