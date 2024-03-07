package com.harris.app.service.saleitem;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.harris.app.model.cache.CacheConstant;
import com.harris.app.model.cache.SaleItemsCache;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.PageResult;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.model.enums.SaleItemStatus;
import com.harris.domain.service.item.SaleItemDomainService;
import com.harris.infra.distributed.cache.DistributedCacheService;
import com.harris.infra.distributed.lock.DistributedLock;
import com.harris.infra.distributed.lock.DistributedLockService;
import com.harris.infra.util.KeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class SaleItemsCacheService {
    private static final String UPDATE_ITEMS_CACHE_LOCK_KEY = "UPDATE_ITEMS_CACHE_LOCK_KEY";
    private final Lock localLock = new ReentrantLock();
    private static final Cache<Long, SaleItemsCache> ITEMS_LOCAL_CACHE =
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
        SaleItemsCache saleItemsCache = ITEMS_LOCAL_CACHE.getIfPresent(activityId);
        if (saleItemsCache != null) {
            if (version == null) {
                return saleItemsCache;
            } else if (version.equals(saleItemsCache.getVersion()) || version < saleItemsCache.getVersion()) {
                return saleItemsCache;
            } else {
                return getDistributedCache(activityId);
            }
        }
        
        return getDistributedCache(activityId);
    }
    
    private SaleItemsCache getDistributedCache(Long activityId) {
        // 1. 从分布式缓存中获取商品列表缓存，key = ITEMS_CACHE_KEY + activityId
        SaleItemsCache distributedItemsCache = distributedCacheService.get(buildItemCacheKey(activityId), SaleItemsCache.class);
        
        // 2. 分布式缓存中也没有，说明 缓存数据尚未生成 或者 缓存失效，尝试更新缓存
        if (distributedItemsCache == null) distributedItemsCache = tryUpdateItemsCache(activityId);
        
        // 3. 分布式缓存中有缓存，并且未处于更新中状态，说明缓存是最新的，可用；将其更新到本地缓存中
        if (distributedItemsCache != null && !distributedItemsCache.isLater()) {
            // 4. 同一时间只允许一个线程更新本地缓存
            boolean lockSuccess = localLock.tryLock();
            if (lockSuccess) {
                try {
                    // 将分布式缓存中的对象更新到本地缓存中
                    ITEMS_LOCAL_CACHE.put(activityId, distributedItemsCache);
                } finally {
                    localLock.unlock();
                }
            }
        }
        
        // 5. 最后返回的：最新状态缓存 / 稍后再试（tryLater） / 不存在（notExist）
        return distributedItemsCache;
    }
    
    public SaleItemsCache tryUpdateItemsCache(Long activityId) {
        // 获取分布式锁实例，防止并发更新商品列表缓存，key = UPDATE_ITEMS_CACHE_LOCK_KEY + activityId
        DistributedLock rLock = distributedLockService.getLock(buildUpdateItemCacheKey(activityId));
        try {
            boolean lockSuccess = rLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!lockSuccess) return new SaleItemsCache().tryLater();
            
            // 从领域服层调用 获取商品列表 方法
            PageQuery pageQuery = new PageQuery().setActivityId(activityId).setStatus(SaleItemStatus.ONLINE.getCode());
            PageResult<SaleItem> itemsPageResult = saleItemDomainService.getItems(pageQuery);
            
            // 如果商品列表不存在，设置状态为不存在；如果存在，设置数据和添加当前时间戳为版本号
            SaleItemsCache saleItemsCache;
            if (itemsPageResult == null) {
                saleItemsCache = new SaleItemsCache().empty();
            } else {
                saleItemsCache = new SaleItemsCache()
                        .setTotal(itemsPageResult.getTotal())
                        .setSaleItems(itemsPageResult.getData())
                        .setVersion(System.currentTimeMillis());
            }
            
            // 更新分布式缓存，key = ITEMS_CACHE_KEY + activityId
            distributedCacheService.put(buildItemCacheKey(activityId), JSON.toJSONString(saleItemsCache), CacheConstant.MINUTES_5);
            
            log.info("分布式商品缓存已更新: [saleItemsCache={}]", saleItemsCache);
            return saleItemsCache;
        } catch (Exception e) {
            log.error("更新分布式商品缓存异常: [activityId: {}] ", activityId, e);
            return new SaleItemsCache().tryLater();
        } finally {
            rLock.unlock();
        }
    }
    
    private String buildItemCacheKey(Long activityId) {
        return KeyUtil.link(CacheConstant.ITEMS_CACHE_KEY, activityId);
    }
    
    private String buildUpdateItemCacheKey(Long activityId) {
        return KeyUtil.link(UPDATE_ITEMS_CACHE_LOCK_KEY, activityId);
    }
}
