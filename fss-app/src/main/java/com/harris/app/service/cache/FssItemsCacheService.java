package com.harris.app.service.cache;

import com.harris.app.model.cache.SaleItemsCache;

public interface FssItemsCacheService {
    SaleItemsCache getItemsCache(Long activityId, Long version);
    SaleItemsCache tryUpdateItemsCacheByLock(Long activityId);
}
