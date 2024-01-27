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
import com.harris.infra.util.LinkUtil;
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

    /**
     * Retrieve sale activities cache, updating from distributed cache if needed.
     *
     * @param pageNumber The requested page number
     * @param version    The version for cache validation
     * @return The activities cache
     */
    public SaleActivitiesCache getActivitiesCache(Integer pageNumber, Long version) {
        // Set default page number if null
        if (pageNumber == null) {
            pageNumber = 1;
        }

        // 1. Try to get from local cache
        SaleActivitiesCache saleActivityCache = activitiesLocalCache.getIfPresent(pageNumber);
        if (saleActivityCache != null) {
            // 1.1 Just return the local cache if no version specified
            if (version == null) {
                log.info("getActivitiesCache, hit local: {}", pageNumber);
                return saleActivityCache;
            }

            // 1.2 Return local cache if version matches or older, meaning it's latest
            if (version.equals(saleActivityCache.getVersion()) || version < saleActivityCache.getVersion()) {
                log.info("activitiesCache, hit local: {},{}", pageNumber, version);
                return saleActivityCache;
            }

            // 1.3 Fetch latest cache from distributed cache if version is newer
            return getLatestDistributedCache(pageNumber);
        }

        // 2. Fetch latest cache from distributed cache if local cache missed
        return getLatestDistributedCache(pageNumber);
    }


    /**
     * Attempt to update the distributed cache with a distributed lock
     *
     * @param pageNumber The page number
     * @return Updated cache or try later indication
     */
    public SaleActivitiesCache tryUpdateActivitiesCache(Integer pageNumber) {
        log.info("tryUpdateActivitiesCache, update remote: {}", pageNumber);

        // 1. Get distributed lock with lock key
        DistributedLock distributedLock = distributedLockService.getDistributedLock(UPDATE_ACTIVITIES_CACHE_LOCK_KEY);

        try {
            // 2. Try to acquire lock, wait for 1 second, timeout after 5 seconds
            boolean lockSuccess = distributedLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!lockSuccess) {
                // 2.1 Return try later response if lock failed
                return new SaleActivitiesCache().tryLater();
            }

            // 3. Get latest activities from domain service
            PageQuery pageQuery = new PageQuery();
            PageResult<SaleActivity> activitiesPageResult = saleActivityDomainService.getActivities(pageQuery);

            // 4. Create a new cache object with the results
            SaleActivitiesCache saleActivitiesCache = (activitiesPageResult != null)
                    ? new SaleActivitiesCache()
                    .setTotal(activitiesPageResult.getTotal())
                    .setSaleActivities(activitiesPageResult.getData())
                    .setVersion(System.currentTimeMillis())
                    : new SaleActivitiesCache().notExist();

            // 5. Update the distributed cache with the new data
            distributedCacheService.put(buildActivityCacheKey(pageNumber),
                    JSON.toJSONString(saleActivitiesCache), CacheConstant.MINUTES_5);
            log.info("tryUpdateActivitiesCache, update remote success: {}", pageNumber);
            return saleActivitiesCache;
        } catch (InterruptedException e) {
            log.error("tryUpdateActivitiesCache, update remote failed: {}", pageNumber);
            return new SaleActivitiesCache().tryLater();
        } finally {
            distributedLock.unlock();
        }
    }

    /**
     * Retrieve the latest cache from distributed storage.
     *
     * @param pageNumber The page number
     * @return Latest cache
     */
    private SaleActivitiesCache getLatestDistributedCache(Integer pageNumber) {
        log.info("getLatestDistributedCache, read remote: {}", pageNumber);

        // 1. Get cache from distributed storage
        SaleActivitiesCache distributedActivitiesCache = distributedCacheService
                .getObject(buildActivityCacheKey(pageNumber), SaleActivitiesCache.class);

        // 2. Try to update cache if cache missed
        if (distributedActivitiesCache == null) {
            distributedActivitiesCache = tryUpdateActivitiesCache(pageNumber);
        }

        // 3. If cache is retrieved or updated successfully, and not try later, update local cache
        if (distributedActivitiesCache != null && !distributedActivitiesCache.isLater()) {
            // 3.1 Try to acquire local lock to update local cache
            boolean lockSuccess = localLock.tryLock();
            if (lockSuccess) {
                try {
                    activitiesLocalCache.put(pageNumber, distributedActivitiesCache);
                    log.info("getLatestDistributedCache, update local: {}", pageNumber);
                } finally {
                    localLock.unlock();
                }
            }
        }

        // 4. Return the latest cache
        return distributedActivitiesCache;
    }

    /**
     * Build the cache key for the specified page number
     *
     * @param pageNumber The page number for cache key
     * @return The constructed cache key
     */
    private String buildActivityCacheKey(Integer pageNumber) {
        return LinkUtil.link(CacheConstant.ACTIVITIES_CACHE_KEY, pageNumber);
    }
}
