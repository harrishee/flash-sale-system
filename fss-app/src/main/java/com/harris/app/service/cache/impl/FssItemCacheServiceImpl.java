package com.harris.app.service.cache.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.harris.app.model.cache.SaleItemCache;
import com.harris.app.service.cache.FssItemCacheService;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.service.SaleItemDomainService;
import com.harris.infra.cache.DistributedCacheService;
import com.harris.infra.lock.DistributedLock;
import com.harris.infra.lock.DistributedLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.harris.app.model.cache.CacheConstant.ITEM_CACHE_KEY;
import static com.harris.app.model.cache.CacheConstant.MINUTES_5;
import static com.harris.infra.util.LinkUtil.link;

@Slf4j
@Service
public class FssItemCacheServiceImpl implements FssItemCacheService {
    private static final String UPDATE_ITEM_CACHE_LOCK_KEY = "UPDATE_ITEM_CACHE_LOCK_KEY_";
    private static final Cache<Long, SaleItemCache> flashItemLocalCache =
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
    private SaleItemDomainService saleItemDomainService;

    @Override
    public SaleItemCache getItemCache(Long itemId, Long version) {
        SaleItemCache saleItemCache = flashItemLocalCache.getIfPresent(itemId);
        if (saleItemCache != null &&
                (version == null || version.equals(saleItemCache.getVersion()) || version < saleItemCache.getVersion())) {
            return saleItemCache;
        }
        return getLatestDistributedCache(itemId);
    }

    @Override
    public SaleItemCache tryUpdateItemCacheByLock(Long itemId) {
        DistributedLock distLock = distributedLockService.getDistributedLock(UPDATE_ITEM_CACHE_LOCK_KEY + itemId);
        try {
            boolean isLocked = distLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLocked) {
                return new SaleItemCache().tryLater();
            }
            SaleItemCache distributedSaleItemCache = distributedCacheService.getObject(buildItemCacheKey(itemId), SaleItemCache.class);
            if (distributedSaleItemCache != null) {
                return distributedSaleItemCache;
            }
            SaleItem saleItem = saleItemDomainService.getItem(itemId);
            SaleItemCache saleItemCache = (saleItem != null)
                    ? new SaleItemCache().with(saleItem).withVersion(System.currentTimeMillis())
                    : new SaleItemCache().notExist();
            distributedCacheService.put(buildItemCacheKey(itemId), JSON.toJSONString(saleItemCache), MINUTES_5);
            return saleItemCache;
        } catch (InterruptedException e) {
            return new SaleItemCache().tryLater();
        } finally {
            distLock.unlock();
        }
    }

    private SaleItemCache getLatestDistributedCache(Long itemId) {
        SaleItemCache distItemCache = distributedCacheService.getObject(buildItemCacheKey(itemId), SaleItemCache.class);
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
