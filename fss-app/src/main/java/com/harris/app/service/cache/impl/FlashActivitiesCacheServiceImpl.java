package com.harris.app.service.cache.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.harris.app.model.cache.FlashActivitiesCache;
import com.harris.app.service.cache.FlashActivitiesCacheService;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.PagesQueryCondition;
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

import static com.harris.app.model.CacheConstant.ACTIVITIES_CACHE_KEY;
import static com.harris.app.model.CacheConstant.MINUTES_5;
import static com.harris.infra.util.StringUtil.link;

@Slf4j
@Service
public class FlashActivitiesCacheServiceImpl implements FlashActivitiesCacheService {
    private static final String UPDATE_ACTIVITIES_CACHE_LOCK_KEY = "UPDATE_ACTIVITIES_CACHE_LOCK_KEY";
    private static final Cache<Integer, FlashActivitiesCache> flashActivitiesLocalCache =
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
    public FlashActivitiesCache getActivitiesCache(Integer pageNumber, Long version) {
        if (pageNumber == null) {
            pageNumber = 1;
        }
        FlashActivitiesCache flashActivityCache = flashActivitiesLocalCache.getIfPresent(pageNumber);
        if (flashActivityCache != null &&
                (version == null || version.equals(flashActivityCache.getVersion()) || version < flashActivityCache.getVersion())) {
            return flashActivityCache;
        }
        return getLatestDistributedCache(pageNumber);
    }

    @Override
    public FlashActivitiesCache tryUpdateActivitiesCacheByLock(Integer pageNumber) {
        DistributedLock distLock = distributedLockService.getDistributedLock(UPDATE_ACTIVITIES_CACHE_LOCK_KEY);
        try {
            boolean isLocked = distLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLocked) {
                return new FlashActivitiesCache().tryLater();
            }
            PagesQueryCondition pagesQueryCondition = new PagesQueryCondition();
            PageResult<FlashActivity> flashActivityPageResult = flashActivityDomainService.getActivities(pagesQueryCondition);
            FlashActivitiesCache flashActivitiesCache = (flashActivityPageResult != null)
                    ? new FlashActivitiesCache()
                    .setTotal(flashActivityPageResult.getTotal())
                    .setFlashActivities(flashActivityPageResult.getData())
                    .setVersion(System.currentTimeMillis())
                    : new FlashActivitiesCache().notExist();
            distributedCacheService.put(buildActivityCacheKey(pageNumber), JSON.toJSONString(flashActivitiesCache), MINUTES_5);
            return flashActivitiesCache;
        } catch (InterruptedException e) {
            return new FlashActivitiesCache().tryLater();
        } finally {
            distLock.unlock();
        }
    }

    private FlashActivitiesCache getLatestDistributedCache(Integer pageNumber) {
        FlashActivitiesCache distActivitiesCache = distributedCacheService.getObject(buildActivityCacheKey(pageNumber), FlashActivitiesCache.class);
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
