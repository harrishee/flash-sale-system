package com.harris.app.service.cache.impl;

import com.harris.app.model.cache.FlashItemCache;
import com.harris.app.service.cache.FlashItemCacheService;

public class FlashItemCacheServiceImpl implements FlashItemCacheService {
    @Override
    public FlashItemCache getItemCache(Long itemId, Long version) {
        return null;
    }

    @Override
    public FlashItemCache tryUpdateItemCacheByLock(Long itemId) {
        return null;
    }
}
