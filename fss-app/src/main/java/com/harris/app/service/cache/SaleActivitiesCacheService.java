package com.harris.app.service.cache;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.harris.app.model.cache.CacheConstant;
import com.harris.app.model.cache.SaleActivitiesCache;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.PageResult;
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
public class SaleActivitiesCacheService {
    private static final String UPDATE_ACTIVITIES_CACHE_LOCK_KEY = "UPDATE_ACTIVITIES_CACHE_LOCK_KEY";
    private final Lock localLock = new ReentrantLock();
    private static final Cache<Integer, SaleActivitiesCache> activitiesLocalCache =
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

    public SaleActivitiesCache getActivitiesCache(Integer pageNumber, Long version) {
        pageNumber = pageNumber == null ? 1 : pageNumber;

        // Try to get from local cache
        SaleActivitiesCache saleActivitiesCache = activitiesLocalCache.getIfPresent(pageNumber);
        if (saleActivitiesCache != null) {
            Long localVersion = saleActivitiesCache.getVersion();

            // Return local cache, if version is null or older or equal to local cache
            if (version == null || version <= localVersion) {
                log.info("getActivitiesCache, hit local cache: {}", pageNumber);
                return saleActivitiesCache;
            }
        } else {

            // Local cache missed or version is newer, need to get from distributed cache
            log.info("getActivitiesCache, miss local cache: {}", pageNumber);
        }

        return getLatestDistributedCache(pageNumber);
    }

    private SaleActivitiesCache getLatestDistributedCache(Integer pageNumber) {
        log.info("getLatestDistributedCache, read distributed cache: {}", pageNumber);

        // Get cache from Redis distributed cache
        SaleActivitiesCache distributedActivitiesCache = distributedCacheService
                .getObject(buildActivityCacheKey(pageNumber), SaleActivitiesCache.class);

        // Try to update cache if not found
        if (distributedActivitiesCache == null) {
            distributedActivitiesCache = tryUpdateActivitiesCache(pageNumber);
        }

        // If cache is found or updated successfully, and not try later, update local cache
        if (distributedActivitiesCache != null && !distributedActivitiesCache.isLater()) {
            boolean lockSuccess = localLock.tryLock();
            if (lockSuccess) {
                try {
                    activitiesLocalCache.put(pageNumber, distributedActivitiesCache);
                    log.info("getLatestDistributedCache, local cache updated: {}", pageNumber);
                } finally {
                    localLock.unlock();
                }
            }
        }

        return distributedActivitiesCache;
    }

    public SaleActivitiesCache tryUpdateActivitiesCache(Integer pageNumber) {
        log.info("tryUpdateActivitiesCache, update distributed cache: {}", pageNumber);

        // Get Redisson distributed lock
        // TODO: UPDATE_ACTIVITIES_CACHE_LOCK_KEY_ + pageNumber ?
        DistributedLock distributedLock = distributedLockService.getDistributedLock(UPDATE_ACTIVITIES_CACHE_LOCK_KEY);

        try {
            // Try to acquire lock, wait for 1 second, timeout after 5 seconds
            boolean lockSuccess = distributedLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!lockSuccess) {
                return new SaleActivitiesCache().tryLater();
            }

            // Get latest activities from domain service
            PageQuery pageQuery = new PageQuery().setPageNumber(pageNumber);
            PageResult<SaleActivity> activitiesPageResult = saleActivityDomainService.getActivities(pageQuery);

            // Create a new cache object with the results
            SaleActivitiesCache saleActivitiesCache;
            if (activitiesPageResult == null) {
                saleActivitiesCache = new SaleActivitiesCache().notExist();
            } else {
                saleActivitiesCache = new SaleActivitiesCache()
                        .setTotal(activitiesPageResult.getTotal())
                        .setSaleActivities(activitiesPageResult.getData())
                        .setVersion(System.currentTimeMillis());
            }

            // Update the result to Redis distributed cache
            distributedCacheService.put(buildActivityCacheKey(pageNumber),
                    JSON.toJSONString(saleActivitiesCache), CacheConstant.MINUTES_5);

            log.info("tryUpdateActivitiesCache, distributed cache updated: {}", pageNumber);
            return saleActivitiesCache;
        } catch (InterruptedException e) {
            log.error("tryUpdateActivitiesCache, distributed cache update error", e);
            return new SaleActivitiesCache().tryLater();
        } finally {
            distributedLock.unlock();
        }
    }

    private String buildActivityCacheKey(Integer pageNumber) {
        return KeyUtil.link(CacheConstant.ACTIVITIES_CACHE_KEY, pageNumber);
    }
}
