package com.harris.app.service.cache.impl;

import com.harris.app.model.cache.FlashActivityCache;
import com.harris.app.service.cache.FlashActivityCacheService;
import org.springframework.stereotype.Service;

@Service
public class FlashActivityCacheServiceImpl implements FlashActivityCacheService {
    @Override
    public FlashActivityCache getActivityCache(Long activityId, Long version) {
        return null;
    }

    @Override
    public FlashActivityCache tryUpdateActivityCacheByLock(Long activityId) {
        return null;
    }

}
