package com.harris.app.service.cache.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.harris.app.model.cache.SaleActivityCache;
import com.harris.app.service.cache.FssActivityCacheService;
import com.harris.domain.model.entity.SaleActivity;
import com.harris.domain.service.FssActivityDomainService;
import com.harris.infra.cache.DistributedCacheService;
import com.harris.infra.lock.DistributedLock;
import com.harris.infra.lock.DistributedLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.harris.app.model.cache.CacheConstant.ACTIVITY_CACHE_KEY;
import static com.harris.app.model.cache.CacheConstant.MINUTES_5;
import static com.harris.infra.util.LinkUtil.link;

@Slf4j
@Service
public class FssActivityCacheServiceImpl implements FssActivityCacheService {
    private static final String UPDATE_ACTIVITY_CACHE_LOCK_KEY = "UPDATE_ACTIVITY_CACHE_LOCK_KEY_";
    private static final Cache<Long, SaleActivityCache> localActivityCache =
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
    private FssActivityDomainService fssActivityDomainService;

    @Override
    public SaleActivityCache getActivityCache(Long activityId, Long version) {
        SaleActivityCache saleActivityCache = localActivityCache.getIfPresent(activityId);
        if (saleActivityCache != null &&
                (version == null || version.equals(saleActivityCache.getVersion()) || version < saleActivityCache.getVersion())) {
            return saleActivityCache;
        }
        return getLatestDistributedCache(activityId);
    }

    @Override
    public SaleActivityCache tryUpdateActivityCacheByLock(Long activityId) {
        DistributedLock distLock = distributedLockService.getDistributedLock(UPDATE_ACTIVITY_CACHE_LOCK_KEY + activityId);
        try {
            boolean isLocked = distLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLocked) {
                return new SaleActivityCache().tryLater();
            }
            SaleActivity saleActivity = fssActivityDomainService.getActivity(activityId);
            SaleActivityCache saleActivityCache = (saleActivity != null)
                    ? new SaleActivityCache().with(saleActivity).withVersion(System.currentTimeMillis())
                    : new SaleActivityCache().notExist();
            distributedCacheService.put(buildActivityCacheKey(activityId), JSON.toJSONString(saleActivityCache), MINUTES_5);
            return saleActivityCache;
        } catch (InterruptedException e) {
            return new SaleActivityCache().tryLater();
        } finally {
            distLock.unlock();
        }
    }

    private SaleActivityCache getLatestDistributedCache(Long activityId) {
        SaleActivityCache distActivityCache = distributedCacheService.getObject(buildActivityCacheKey(activityId), SaleActivityCache.class);
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
