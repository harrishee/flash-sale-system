package com.harris.app.service.cache;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.harris.app.model.cache.CacheConstant;
import com.harris.app.model.cache.SaleItemsCache;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.model.enums.SaleItemStatus;
import com.harris.domain.service.SaleItemDomainService;
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
public class SaleItemsCacheService {
    private static final String UPDATE_ITEMS_CACHE_LOCK_KEY = "UPDATE_ITEMS_CACHE_LOCK_KEY_";
    private final Lock localLock = new ReentrantLock();
    private static final Cache<Long, SaleItemsCache> itemsLocalCache =
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

    public SaleItemsCache getItemsCache(Long activityId, Long version) {
        if (activityId == null) {
            return null;
        }

        SaleItemsCache saleItemsCache = itemsLocalCache.getIfPresent(activityId);
        if (saleItemsCache != null) {
            if (version == null) {
                log.info("getItemsCache, hit local: {}", activityId);
                return saleItemsCache;
            }

            if (version.equals(saleItemsCache.getVersion()) || version < saleItemsCache.getVersion()) {
                log.info("getItemsCache, hit local: {},{}", activityId, version);
                return saleItemsCache;
            }

            return getLatestDistributedCache(activityId);
        }

        return getLatestDistributedCache(activityId);
    }

    public SaleItemsCache tryUpdateItemsCache(Long itemId) {
        log.info("tryUpdateItemsCache, update remote: {}", itemId);

        DistributedLock distributedLock = distributedLockService
                .getDistributedLock(UPDATE_ITEMS_CACHE_LOCK_KEY + itemId);

        try {
            boolean lockSuccess = distributedLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!lockSuccess) {
                return new SaleItemsCache().tryLater();
            }

            PageQuery pageQuery = new PageQuery().setActivityId(itemId).setStatus(SaleItemStatus.ONLINE.getCode());
            PageResult<SaleItem> itemsPageResult = saleItemDomainService.getItems(pageQuery);

            SaleItemsCache saleItemsCache = (itemsPageResult != null)
                    ? new SaleItemsCache()
                    .setTotal(itemsPageResult.getTotal())
                    .setSaleItems(itemsPageResult.getData())
                    .setVersion(System.currentTimeMillis())
                    : new SaleItemsCache().empty();

            distributedCacheService.put(buildItemCacheKey(itemId),
                    JSON.toJSONString(saleItemsCache), CacheConstant.MINUTES_5);
            log.info("tryUpdateItemsCache, update remote success: {}", itemId);
            return saleItemsCache;
        } catch (Exception e) {
            log.error("tryUpdateItemsCache, update remote failed: {}", itemId);
            return new SaleItemsCache().tryLater();
        } finally {
            distributedLock.unlock();
        }
    }

    private SaleItemsCache getLatestDistributedCache(Long itemId) {
        log.info("getLatestDistributedCache, read remote: {}", itemId);

        SaleItemsCache distributedItemsCache = distributedCacheService
                .getObject(buildItemCacheKey(itemId), SaleItemsCache.class);
        if (distributedItemsCache == null) {
            distributedItemsCache = tryUpdateItemsCache(itemId);
        }

        if (distributedItemsCache != null && !distributedItemsCache.isLater()) {
            boolean lockSuccess = localLock.tryLock();
            if (lockSuccess) {
                try {
                    itemsLocalCache.put(itemId, distributedItemsCache);
                    log.info("getLatestDistributedCache, update local: {}", itemId);
                } finally {
                    localLock.unlock();
                }
            }
        }

        return distributedItemsCache;
    }

    private String buildItemCacheKey(Long itemId) {
        return LinkUtil.link(CacheConstant.ITEMS_CACHE_KEY + itemId);
    }
}
