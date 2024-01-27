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
import com.harris.infra.util.LinkUtil;
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

        SaleActivityCache saleActivityCache = activityLocalCache.getIfPresent(activityId);
        if (saleActivityCache != null) {
            if (version == null) {
                log.info("getActivityCache, hit local: {}", activityId);
                return saleActivityCache;
            }

            if (version.equals(saleActivityCache.getVersion()) || version < saleActivityCache.getVersion()) {
                log.info("getActivityCache, hit local: {},{}", activityId, version);
                return saleActivityCache;
            }

            return getLatestDistributedCache(activityId);
        }

        return getLatestDistributedCache(activityId);
    }

    public SaleActivityCache tryUpdateActivityCache(Long activityId) {
        log.info("tryUpdateActivityCache, update remote: {}", activityId);

        DistributedLock distributedLock = distributedLockService
                .getDistributedLock(UPDATE_ACTIVITY_CACHE_LOCK_KEY + activityId);

        try {
            boolean lockSuccess = distributedLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!lockSuccess) {
                return new SaleActivityCache().tryLater();
            }

            SaleActivity saleActivity = saleActivityDomainService.getActivity(activityId);
            SaleActivityCache saleActivityCache = (saleActivity != null)
                    ? new SaleActivityCache().with(saleActivity).withVersion(System.currentTimeMillis())
                    : new SaleActivityCache().notExist();

            distributedCacheService.put(buildActivityCacheKey(activityId),
                    JSON.toJSONString(saleActivityCache), CacheConstant.MINUTES_5);
            log.info("tryUpdateActivityCache, update remote success: {}", activityId);
            return saleActivityCache;
        } catch (InterruptedException e) {
            log.error("tryUpdateActivityCache, update remote failed: {}", activityId);
            return new SaleActivityCache().tryLater();
        } finally {
            distributedLock.unlock();
        }
    }

    private SaleActivityCache getLatestDistributedCache(Long activityId) {
        log.info("getLatestDistributedCache, read remote: {}", activityId);

        SaleActivityCache distributedActivitiesCache = distributedCacheService
                .getObject(buildActivityCacheKey(activityId), SaleActivityCache.class);
        if (distributedActivitiesCache == null) {
            distributedActivitiesCache = tryUpdateActivityCache(activityId);
        }

        if (distributedActivitiesCache != null && !distributedActivitiesCache.isLater()) {
            boolean lockSuccess = localLock.tryLock();
            if (lockSuccess) {
                try {
                    activityLocalCache.put(activityId, distributedActivitiesCache);
                    log.info("getLatestDistributedCache, update local: {}", activityId);
                } finally {
                    localLock.unlock();
                }
            }
        }

        return distributedActivitiesCache;
    }

    private String buildActivityCacheKey(Long activityId) {
        return LinkUtil.link(CacheConstant.ACTIVITY_CACHE_KEY, activityId);
    }
}
