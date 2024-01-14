package com.harris.app.service.cache.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.harris.app.model.cache.FlashItemCache;
import com.harris.app.service.cache.FlashItemCacheService;
import com.harris.domain.model.entity.FlashItem;
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

import static com.harris.app.model.CacheConstant.ITEM_CACHE_KEY;
import static com.harris.app.model.CacheConstant.MINUTES_5;
import static com.harris.infra.util.StringUtil.link;

@Slf4j
@Service
public class FlashItemCacheServiceImpl implements FlashItemCacheService {
    private static final String UPDATE_ITEM_CACHE_LOCK_KEY = "UPDATE_ITEM_CACHE_LOCK_KEY_";
    private static final Cache<Long, FlashItemCache> flashItemLocalCache =
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
    public FlashItemCache getItemCache(Long itemId, Long version) {
        FlashItemCache flashItemCache = flashItemLocalCache.getIfPresent(itemId);
        if (flashItemCache != null &&
                (version == null || version.equals(flashItemCache.getVersion()) || version < flashItemCache.getVersion())) {
            return flashItemCache;
        }
        return getLatestDistributedCache(itemId);
    }

    @Override
    public FlashItemCache tryUpdateItemCacheByLock(Long itemId) {
        DistributedLock distLock = distributedLockService.getDistributedLock(UPDATE_ITEM_CACHE_LOCK_KEY + itemId);
        try {
            boolean isLocked = distLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLocked) {
                return new FlashItemCache().tryLater();
            }
            FlashItemCache distributedFlashItemCache = distributedCacheService.getObject(buildItemCacheKey(itemId), FlashItemCache.class);
            if (distributedFlashItemCache != null) {
                return distributedFlashItemCache;
            }
            FlashItem flashItem = flashItemDomainService.getItem(itemId);
            FlashItemCache flashItemCache = (flashItem != null)
                    ? new FlashItemCache().with(flashItem).withVersion(System.currentTimeMillis())
                    : new FlashItemCache().notExist();
            distributedCacheService.put(buildItemCacheKey(itemId), JSON.toJSONString(flashItemCache), MINUTES_5);
            return flashItemCache;
        } catch (InterruptedException e) {
            return new FlashItemCache().tryLater();
        } finally {
            distLock.unlock();
        }
    }

    private FlashItemCache getLatestDistributedCache(Long itemId) {
        FlashItemCache distItemCache = distributedCacheService.getObject(buildItemCacheKey(itemId), FlashItemCache.class);
        if (distItemCache == null) {
            distItemCache = tryUpdateItemCacheByLock(itemId);
        }
        if (distItemCache != null && !distItemCache.isLater()) {
            boolean isLocked = localLock.tryLock();
            if (isLocked) {
                try {
                    flashItemLocalCache.put(itemId, distItemCache);
                } finally {
                    localLock.unlock();
                }
            }
        }
        return distItemCache;
    }

    private String buildItemCacheKey(Long itemId) {
        return link(ITEM_CACHE_KEY, itemId);
    }
}
