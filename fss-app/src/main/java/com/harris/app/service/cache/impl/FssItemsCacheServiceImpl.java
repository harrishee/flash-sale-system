package com.harris.app.service.cache.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.harris.app.model.cache.SaleItemsCache;
import com.harris.app.service.cache.FssItemsCacheService;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.model.enums.SaleItemStatus;
import com.harris.domain.service.FssItemDomainService;
import com.harris.infra.cache.DistributedCacheService;
import com.harris.infra.lock.DistributedLock;
import com.harris.infra.lock.DistributedLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.harris.app.model.cache.CacheConstant.ITEMS_CACHE_KEY;
import static com.harris.app.model.cache.CacheConstant.MINUTES_5;
import static com.harris.infra.util.LinkUtil.link;

@Slf4j
@Service
public class FssItemsCacheServiceImpl implements FssItemsCacheService {
    private static final String UPDATE_ITEMS_CACHE_LOCK_KEY = "UPDATE_ITEMS_CACHE_LOCK_KEY_";
    private final static Cache<Long, SaleItemsCache> flashItemsLocalCache =
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
    private FssItemDomainService fssItemDomainService;

    @Override
    public SaleItemsCache getItemsCache(Long activityId, Long version) {
        SaleItemsCache flashItemCache = flashItemsLocalCache.getIfPresent(activityId);
        if (flashItemCache != null &&
                (version == null || version.equals(flashItemCache.getVersion()) || version < flashItemCache.getVersion())) {
            return flashItemCache;
        }
        return getLatestDistributedCache(activityId);
    }

    @Override
    public SaleItemsCache tryUpdateItemsCacheByLock(Long activityId) {
        DistributedLock distLock = distributedLockService.getDistributedLock(UPDATE_ITEMS_CACHE_LOCK_KEY + activityId);
        try {
            boolean isLocked = distLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLocked) {
                return new SaleItemsCache().tryLater();
            }
            PageQueryCondition pageQueryCondition = new PageQueryCondition();
            pageQueryCondition.setActivityId(activityId);
            pageQueryCondition.setStatus(SaleItemStatus.ONLINE.getCode());
            PageResult<SaleItem> flashItemPageResult = fssItemDomainService.getItems(pageQueryCondition);
            SaleItemsCache saleItemsCache = (flashItemPageResult != null)
                    ? new SaleItemsCache()
                    .setTotal(flashItemPageResult.getTotal())
                    .setSaleItems(flashItemPageResult.getData())
                    .setVersion(System.currentTimeMillis())
                    : new SaleItemsCache().empty();
            distributedCacheService.put(buildItemCacheKey(activityId), JSON.toJSONString(saleItemsCache), MINUTES_5);
            return saleItemsCache;
        } catch (Exception e) {
            return new SaleItemsCache().tryLater();
        } finally {
            distLock.unlock();
        }
    }

    private SaleItemsCache getLatestDistributedCache(Long activityId) {
        SaleItemsCache distItemsCache = distributedCacheService.getObject(buildItemCacheKey(activityId), SaleItemsCache.class);
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
