package com.harris.app.service.cache.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.harris.app.model.cache.FlashActivityCache;
import com.harris.app.service.cache.FlashActivityCacheService;
import com.harris.domain.model.entity.FlashActivity;
import com.harris.domain.service.FlashActivityDomainService;
import com.harris.infra.cache.DistributedCacheService;
import com.harris.infra.lock.DistributedLock;
import com.harris.infra.lock.DistributedLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.harris.app.model.CacheConstant.ACTIVITY_CACHE_KEY;
import static com.harris.app.model.CacheConstant.MINUTES_5;
import static com.harris.infra.util.StringUtil.link;

@Slf4j
@Service
public class FlashActivityCacheServiceImpl implements FlashActivityCacheService {
    private static final String UPDATE_ACTIVITY_CACHE_LOCK_KEY = "UPDATE_ACTIVITY_CACHE_LOCK_KEY_";
    private static final Cache<Long, FlashActivityCache> localActivityCache =
            CacheBuilder.newBuilder()
                    .initialCapacity(10)
                    .concurrencyLevel(5)
                    .expireAfterWrite(10, TimeUnit.SECONDS)
                    .build();
    private final Lock localLock = new ReentrantLock();

    @Resource
    private DistributedCacheService distributedCacheService;

    @Resource
    private DistributedLockService distributedLockService;

    @Resource
    private FlashActivityDomainService flashActivityDomainService;

    @Override
    public FlashActivityCache getActivityCache(Long activityId, Long version) {
        FlashActivityCache flashActivityCache = localActivityCache.getIfPresent(activityId);
        if (flashActivityCache != null &&
                (version == null || version.equals(flashActivityCache.getVersion()) || version < flashActivityCache.getVersion())) {
            return flashActivityCache;
        }
        return getLatestDistributedCache(activityId);
    }

    @Override
    public FlashActivityCache tryUpdateActivityCacheByLock(Long activityId) {
        DistributedLock distLock = distributedLockService.getDistributedLock(UPDATE_ACTIVITY_CACHE_LOCK_KEY + activityId);
        try {
            boolean isLocked = distLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLocked) {
                return new FlashActivityCache().tryLater();
            }
            FlashActivity flashActivity = flashActivityDomainService.getActivity(activityId);
            FlashActivityCache flashActivityCache = (flashActivity != null)
                    ? new FlashActivityCache().with(flashActivity).withVersion(System.currentTimeMillis())
                    : new FlashActivityCache().notExist();
            distributedCacheService.put(buildActivityCacheKey(activityId), JSON.toJSONString(flashActivityCache), MINUTES_5);
            return flashActivityCache;
        } catch (InterruptedException e) {
            return new FlashActivityCache().tryLater();
        } finally {
            distLock.unlock();
        }
    }

    private FlashActivityCache getLatestDistributedCache(Long activityId) {
        FlashActivityCache distActivityCache = distributedCacheService.getObject(buildActivityCacheKey(activityId), FlashActivityCache.class);
        if (distActivityCache == null) {
            distActivityCache = tryUpdateActivityCacheByLock(activityId);
        }
        if (distActivityCache != null && !distActivityCache.isLater()) {
            boolean isLocked = localLock.tryLock();
            if (isLocked) {
                try {
                    localActivityCache.put(activityId, distActivityCache);
                } finally {
                    localLock.unlock();
                }
            }
        }
        return distActivityCache;
    }

    private String buildActivityCacheKey(Long activityId) {
        return link(ACTIVITY_CACHE_KEY, activityId);
    }
}
