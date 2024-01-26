package com.harris.app.service.cache.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.harris.app.model.cache.SaleActivitiesCache;
import com.harris.app.service.cache.FssActivitiesCacheService;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleActivity;
import com.harris.domain.service.SaleActivityDomainService;
import com.harris.infra.cache.DistributedCacheService;
import com.harris.infra.lock.DistributedLock;
import com.harris.infra.lock.DistributedLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.harris.app.model.cache.CacheConstant.ACTIVITIES_CACHE_KEY;
import static com.harris.app.model.cache.CacheConstant.MINUTES_5;
import static com.harris.infra.util.LinkUtil.link;

@Slf4j
@Service
public class FssActivitiesCacheServiceImpl implements FssActivitiesCacheService {
    private static final String UPDATE_ACTIVITIES_CACHE_LOCK_KEY = "UPDATE_ACTIVITIES_CACHE_LOCK_KEY";
    private static final Cache<Integer, SaleActivitiesCache> flashActivitiesLocalCache =
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
    private SaleActivityDomainService saleActivityDomainService;

    @Override
    public SaleActivitiesCache getActivitiesCache(Integer pageNumber, Long version) {
        if (pageNumber == null) {
            pageNumber = 1;
        }
        SaleActivitiesCache flashActivityCache = flashActivitiesLocalCache.getIfPresent(pageNumber);
        if (flashActivityCache != null &&
                (version == null || version.equals(flashActivityCache.getVersion()) || version < flashActivityCache.getVersion())) {
            return flashActivityCache;
        }
        return getLatestDistributedCache(pageNumber);
    }

    @Override
    public SaleActivitiesCache tryUpdateActivitiesCacheByLock(Integer pageNumber) {
        DistributedLock distLock = distributedLockService.getDistributedLock(UPDATE_ACTIVITIES_CACHE_LOCK_KEY);
        try {
            boolean isLocked = distLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLocked) {
                return new SaleActivitiesCache().tryLater();
            }
            PageQueryCondition pageQueryCondition = new PageQueryCondition();
            PageResult<SaleActivity> flashActivityPageResult = saleActivityDomainService.getActivities(pageQueryCondition);
            SaleActivitiesCache saleActivitiesCache = (flashActivityPageResult != null)
                    ? new SaleActivitiesCache()
                    .setTotal(flashActivityPageResult.getTotal())
                    .setSaleActivities(flashActivityPageResult.getData())
                    .setVersion(System.currentTimeMillis())
                    : new SaleActivitiesCache().notExist();
            distributedCacheService.put(buildActivityCacheKey(pageNumber), JSON.toJSONString(saleActivitiesCache), MINUTES_5);
            return saleActivitiesCache;
        } catch (InterruptedException e) {
            return new SaleActivitiesCache().tryLater();
        } finally {
            distLock.unlock();
        }
    }

    private SaleActivitiesCache getLatestDistributedCache(Integer pageNumber) {
        SaleActivitiesCache distActivitiesCache = distributedCacheService.getObject(buildActivityCacheKey(pageNumber), SaleActivitiesCache.class);
        if (distActivitiesCache == null) {
            distActivitiesCache = tryUpdateActivitiesCacheByLock(pageNumber);
        }
        if (distActivitiesCache != null && !distActivitiesCache.isLater()) {
            boolean isLocked = localLock.tryLock();
            if (isLocked) {
                try {
                    flashActivitiesLocalCache.put(pageNumber, distActivitiesCache);
                } finally {
                    localLock.unlock();
                }
            }
        }
        return distActivitiesCache;
    }

    private String buildActivityCacheKey(Integer pageNumber) {
        return link(ACTIVITIES_CACHE_KEY, pageNumber);
    }
}
