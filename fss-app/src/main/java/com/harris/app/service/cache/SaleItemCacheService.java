package com.harris.app.service.cache;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.harris.app.model.cache.CacheConstant;
import com.harris.app.model.cache.SaleItemCache;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.service.SaleItemDomainService;
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
public class SaleItemCacheService {
    private static final String UPDATE_ITEM_CACHE_LOCK_KEY = "UPDATE_ITEM_CACHE_LOCK_KEY_";
    private final Lock localLock = new ReentrantLock();
    private static final Cache<Long, SaleItemCache> itemLocalCache =
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
    private SaleItemDomainService saleItemDomainService;

    /**
     * Retrieve sale item cache, updating from distributed cache if needed.
     *
     * @param itemId  The requested item id
     * @param version The version for cache validation
     * @return The item cache
     */
    public SaleItemCache getItemCache(Long itemId, Long version) {
        if (itemId == null) {
            return null;
        }

        SaleItemCache saleItemCache = itemLocalCache.getIfPresent(itemId);
        if (saleItemCache != null) {
            if (version == null) {
                log.info("getItemCache, hit local: {}", itemId);
                return saleItemCache;
            }

            if (version.equals(saleItemCache.getVersion()) || version < saleItemCache.getVersion()) {
                log.info("getItemCache, hit local: {},{}", itemId, version);
                return saleItemCache;
            }

            return getLatestDistributedCache(itemId);
        }

        return getLatestDistributedCache(itemId);
    }

    /**
     * Attempt to update distributed cache with a distributed lock
     *
     * @param itemId The requested item id
     * @return Updated cache or try later indication
     */
    public SaleItemCache tryUpdateItemCache(Long itemId) {
        log.info("tryUpdateItemCache, update remote: {}", itemId);

        DistributedLock distributedLock = distributedLockService
                .getDistributedLock(UPDATE_ITEM_CACHE_LOCK_KEY + itemId);

        try {
            boolean lockSuccess = distributedLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!lockSuccess) {
                return new SaleItemCache().tryLater();
            }

            // TODO: look later
            SaleItemCache distributedItemCache = distributedCacheService
                    .getObject(buildItemCacheKey(itemId), SaleItemCache.class);
            if (distributedItemCache != null) {
                return distributedItemCache;
            }

            SaleItem saleItem = saleItemDomainService.getItem(itemId);
            SaleItemCache saleItemCache = (saleItem != null)
                    ? new SaleItemCache().with(saleItem).withVersion(System.currentTimeMillis())
                    : new SaleItemCache().notExist();

            distributedCacheService.put(buildItemCacheKey(itemId),
                    JSON.toJSONString(saleItemCache), CacheConstant.MINUTES_5);
            log.info("tryUpdateItemCache, update remote success: {}", itemId);
            return saleItemCache;
        } catch (InterruptedException e) {
            log.error("tryUpdateItemCache, update remote failed: {}", itemId);
            return new SaleItemCache().tryLater();
        } finally {
            distributedLock.unlock();
        }
    }

    /**
     * Retrieve the latest cache from distributed storage.
     *
     * @param itemId The requested item id
     * @return The item cache
     */
    private SaleItemCache getLatestDistributedCache(Long itemId) {
        log.info("getLatestDistributedCache, read remote: {}", itemId);

        SaleItemCache distributedItemCache = distributedCacheService
                .getObject(buildItemCacheKey(itemId), SaleItemCache.class);
        if (distributedItemCache == null) {
            distributedItemCache = tryUpdateItemCache(itemId);
        }

        if (distributedItemCache != null && !distributedItemCache.isLater()) {
            boolean lockSuccess = localLock.tryLock();
            if (lockSuccess) {
                try {
                    itemLocalCache.put(itemId, distributedItemCache);
                    log.info("getLatestDistributedCache, update local: {}", itemId);
                } finally {
                    localLock.unlock();
                }
            }
        }

        return distributedItemCache;
    }

    private String buildItemCacheKey(Long itemId) {
        return KeyUtil.link(CacheConstant.ITEM_CACHE_KEY, itemId);
    }
}
