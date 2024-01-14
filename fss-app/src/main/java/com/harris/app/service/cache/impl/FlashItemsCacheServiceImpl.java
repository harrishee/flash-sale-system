package com.harris.app.service.cache.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.harris.app.model.cache.FlashItemsCache;
import com.harris.app.service.cache.FlashItemsCacheService;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashItem;
import com.harris.domain.model.enums.FlashItemStatus;
import com.harris.domain.service.FlashItemDomainService;
import com.harris.infra.cache.DistributedCacheService;
import com.harris.infra.lock.DistributedLock;
import com.harris.infra.lock.DistributedLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.harris.app.model.CacheConstant.ITEMS_CACHE_KEY;
import static com.harris.app.model.CacheConstant.MINUTES_5;
import static com.harris.infra.util.StringUtil.link;

@Slf4j
@Service
public class FlashItemsCacheServiceImpl implements FlashItemsCacheService {
    private static final String UPDATE_ITEMS_CACHE_LOCK_KEY = "UPDATE_ITEMS_CACHE_LOCK_KEY_";
    private final static Cache<Long, FlashItemsCache> flashItemsLocalCache =
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
    private FlashItemDomainService flashItemDomainService;

    @Override
    public FlashItemsCache getItemsCache(Long activityId, Long version) {
        FlashItemsCache flashItemCache = flashItemsLocalCache.getIfPresent(activityId);
        if (flashItemCache != null &&
                (version == null || version.equals(flashItemCache.getVersion()) || version < flashItemCache.getVersion())) {
            return flashItemCache;
        }
        return getLatestDistributedCache(activityId);
    }

    @Override
    public FlashItemsCache tryUpdateItemsCacheByLock(Long activityId) {
        DistributedLock distLock = distributedLockService.getDistributedLock(UPDATE_ITEMS_CACHE_LOCK_KEY + activityId);
        try {
            boolean isLocked = distLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLocked) {
                return new FlashItemsCache().tryLater();
            }
            PagesQueryCondition pagesQueryCondition = new PagesQueryCondition();
            pagesQueryCondition.setActivityId(activityId);
            pagesQueryCondition.setStatus(FlashItemStatus.ONLINE.getCode());
            PageResult<FlashItem> flashItemPageResult = flashItemDomainService.getItems(pagesQueryCondition);
            FlashItemsCache flashItemsCache = (flashItemPageResult != null)
                    ? new FlashItemsCache()
                    .setTotal(flashItemPageResult.getTotal())
                    .setFlashItems(flashItemPageResult.getData())
                    .setVersion(System.currentTimeMillis())
                    : new FlashItemsCache().empty();
            distributedCacheService.put(buildItemCacheKey(activityId), JSON.toJSONString(flashItemsCache), MINUTES_5);
            return flashItemsCache;
        } catch (Exception e) {
            return new FlashItemsCache().tryLater();
        } finally {
            distLock.unlock();
        }
    }

    private FlashItemsCache getLatestDistributedCache(Long activityId) {
        FlashItemsCache distItemsCache = distributedCacheService.getObject(buildItemCacheKey(activityId), FlashItemsCache.class);
        if (distItemsCache == null) {
            distItemsCache = tryUpdateItemsCacheByLock(activityId);
        }
        if (distItemsCache != null && !distItemsCache.isLater()) {
            boolean isLocked = localLock.tryLock();
            if (isLocked) {
                try {
                    flashItemsLocalCache.put(activityId, distItemsCache);
                } finally {
                    localLock.unlock();
                }
            }
        }
        return distItemsCache;
    }

    private String buildItemCacheKey(Long activityId) {
        return link(ITEMS_CACHE_KEY + activityId);
    }
}
