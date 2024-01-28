package com.harris.app.service.cache;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.harris.app.model.cache.CacheConstant;
import com.harris.app.model.cache.SaleActivityCache;
import com.harris.domain.model.entity.SaleActivity;
import com.harris.domain.service.SaleActivityDomainService;
import com.harris.infra.cache.DistributedCacheService;
import com.harris.infra.lock.DistributedLock;
import com.harris.infra.lock.DistributedLockService;
import com.harris.infra.util.KeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class SaleActivityCacheService {
    private static final String UPDATE_ACTIVITY_CACHE_LOCK_KEY = "UPDATE_ACTIVITY_CACHE_LOCK_KEY_";
    private final Lock localLock = new ReentrantLock();
    private static final Cache<Long, SaleActivityCache> activityLocalCache =
            CacheBuilder.newBuilder()
                    .initialCapacity(10)
                    .concurrencyLevel(5)
                    .expireAfterWrite(10, TimeUnit.SECONDS)
                    .build();

    @Resource
    private DistributedCacheService distributedCacheService;

    @Resource
    private DistributedLockService distributedLockService;

    @Resource
    private SaleActivityDomainService saleActivityDomainService;

    public SaleActivityCache getActivityCache(Long activityId, Long version) {
        if (activityId == null) {
            return null;
        }

        // Try to get from local cache
        SaleActivityCache saleActivityCache = activityLocalCache.getIfPresent(activityId);
        if (saleActivityCache != null) {
            Long localVersion = saleActivityCache.getVersion();
            if (version == null || version <= localVersion) {
                log.info("getActivityCache, hit local cache: {}", activityId);
                return saleActivityCache;
            }
        } else {
            // Local cache missed or version is newer, need to get from distributed cache
            log.info("getActivityCache, miss local cache: {}", activityId);
        }

        return getLatestDistributedCache(activityId);
    }

    private SaleActivityCache getLatestDistributedCache(Long activityId) {
        log.info("getLatestDistributedCache, read distributed cache: {}", activityId);

        // Get cache from Redis distributed cache
        SaleActivityCache distributedActivityCache = distributedCacheService
                .getObject(buildActivityCacheKey(activityId), SaleActivityCache.class);

        // Try to update cache if not found
        if (distributedActivityCache == null) {
            distributedActivityCache = tryUpdateActivityCache(activityId);
        }

        // If cache is found or updated successfully, and not try later, update local cache
        if (distributedActivityCache != null && !distributedActivityCache.isLater()) {
            boolean lockSuccess = localLock.tryLock();
            if (lockSuccess) {
                try {
                    activityLocalCache.put(activityId, distributedActivityCache);
                    log.info("getLatestDistributedCache, local cache updated: {}", activityId);
                } finally {
                    localLock.unlock();
                }
            }
        }

        return distributedActivityCache;
    }

    public SaleActivityCache tryUpdateActivityCache(Long activityId) {
        log.info("tryUpdateActivityCache, update distributed cache: {}", activityId);

        // Get Redisson distributed lock
        DistributedLock distributedLock = distributedLockService.getDistributedLock(UPDATE_ACTIVITY_CACHE_LOCK_KEY + activityId);

        try {
            boolean lockSuccess = distributedLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!lockSuccess) {
                return new SaleActivityCache().tryLater();
            }

            // Get latest activity from domain service
            SaleActivity saleActivity = saleActivityDomainService.getActivity(activityId);

            // Create a new cache object with the results
            SaleActivityCache saleActivityCache = saleActivity != null
                    ? new SaleActivityCache().with(saleActivity).withVersion(System.currentTimeMillis())
                    : new SaleActivityCache().notExist();

            // Update the result to Redis distributed cache
            distributedCacheService.put(buildActivityCacheKey(activityId),
                    JSON.toJSONString(saleActivityCache), CacheConstant.MINUTES_5);

            log.info("tryUpdateActivityCache, distributed cache updated: {}", activityId);
            return saleActivityCache;
        } catch (InterruptedException e) {
            log.error("tryUpdateActivityCache, distributed cache update error", e);
            return new SaleActivityCache().tryLater();
        } finally {
            distributedLock.unlock();
        }
    }

    private String buildActivityCacheKey(Long activityId) {
        return KeyUtil.link(CacheConstant.ACTIVITY_CACHE_KEY, activityId);
    }
}
