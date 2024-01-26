package com.harris.app.service.cache;

import com.harris.app.model.cache.SaleItemCache;

public interface FssItemCacheService {
    SaleItemCache getItemCache(Long itemId, Long version);

    SaleItemCache tryUpdateItemCacheByLock(Long itemId);
}
