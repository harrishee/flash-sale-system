package com.harris.app.service.cache;

import com.harris.app.model.cache.FlashItemCache;

public interface FlashItemCacheService {
    FlashItemCache getItemCache(Long itemId, Long version);

    FlashItemCache tryUpdateItemCacheByLock(Long itemId);
}
