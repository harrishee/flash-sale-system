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
    private static final String UPDATE_ITEM_CACHE_LOCK_KEY = "UPDATE_ITEM_CACHE_LOCK_KEY";
    private final Lock localLock = new ReentrantLock();
    private static final Cache<Long, SaleItemCache> ITEM_LOCAL_CACHE =
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
    
    public SaleItemCache getItemCache(Long itemId, Long version) {
        SaleItemCache saleItemCache = ITEM_LOCAL_CACHE.getIfPresent(itemId);
        
        // 1.1 如果本地有缓存，进一步判断版本号，否则直接从分布式缓存获取
        if (saleItemCache != null) {
            if (version == null) {
                // 2.1 无版本号情况下直接返回本地缓存
                return saleItemCache;
            } else if (version.equals(saleItemCache.getVersion()) || version < saleItemCache.getVersion()) {
                // 2.2 有版本号情况下，版本号一致 或者 本地缓存版本号大于提供版本号，说明本地缓存是最新的
                return saleItemCache;
            } else {
                // 2.3 本地缓存版本号小于提供的版本号，说明本地缓存不是最新的，从分布式缓存获取
                return getDistributedCache(itemId);
            }
        }
        
        // 1.2 本地没有缓存，直接从分布式缓存获取
        return getDistributedCache(itemId);
    }
    
    private SaleItemCache getDistributedCache(Long itemId) {
        // 1. 从分布式缓存中获取活动缓存，key = ITEM_CACHE_KEY + itemId
        SaleItemCache distributedItemCache = distributedCacheService.get(buildItemCacheKey(itemId), SaleItemCache.class);
        
        // 2. 分布式缓存中也没有，说明 缓存数据尚未生成 或者 缓存失效，尝试更新缓存
        if (distributedItemCache == null) distributedItemCache = tryUpdateItemCache(itemId);
        
        // 3. 分布式缓存中有缓存，并且未处于更新中状态，说明缓存是最新的，可用；将其更新到本地缓存中
        if (distributedItemCache != null && !distributedItemCache.isLater()) {
            // 4. 同一时间只允许一个线程更新本地缓存
            boolean lockSuccess = localLock.tryLock();
            if (lockSuccess) {
                try {
                    ITEM_LOCAL_CACHE.put(itemId, distributedItemCache);
                } finally {
                    localLock.unlock();
                }
            }
        }
        
        // 5. 最后返回的：最新状态缓存 / 稍后再试（tryLater） / 不存在（notExist）
        return distributedItemCache;
    }
    
    public SaleItemCache tryUpdateItemCache(Long itemId) {
        // 获取分布式锁实例，防止并发更新商品缓存，key = UPDATE_ITEM_CACHE_LOCK_KEY + itemId
        DistributedLock rLock = distributedLockService.getLock(buildUpdateItemCacheKey(itemId));
        try {
            boolean lockSuccess = rLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!lockSuccess) return new SaleItemCache().tryLater();
            
            // 先尝试从分布式缓存中获取商品缓存对象，如果已经存在则直接返回，避免不必要的更新
            SaleItemCache redisItemCache = distributedCacheService.get(buildItemCacheKey(itemId), SaleItemCache.class);
            if (redisItemCache != null) return redisItemCache;
            
            // 再从领域服层调用 获取商品 方法
            SaleItem saleItem = saleItemDomainService.getItem(itemId);
            
            // 如果商品不存在，设置状态为不存在；如果存在，设置数据和添加当前时间戳为版本号
            SaleItemCache saleItemCache;
            if (saleItem == null) {
                saleItemCache = new SaleItemCache().notExist();
            } else {
                saleItemCache = new SaleItemCache().with(saleItem).withVersion(System.currentTimeMillis());
            }
            // 更新商品的分布式缓存，key = ITEM_CACHE_KEY + itemId
            distributedCacheService.put(buildItemCacheKey(itemId), JSON.toJSONString(saleItemCache), CacheConstant.MINUTES_5);
            
            // log.info("分布式商品缓存已更新: [saleActivityCache={}]", saleActivityCache);
            return saleItemCache;
        } catch (InterruptedException e) {
            log.error("更新分布式商品缓存异常: [itemId: {}] ", itemId, e);
            return new SaleItemCache().tryLater();
        } finally {
            rLock.unlock();
        }
    }
    
    private String buildItemCacheKey(Long itemId) {
        return KeyUtil.link(CacheConstant.ITEM_CACHE_KEY, itemId);
    }
    
    private String buildUpdateItemCacheKey(Long itemId) {
        return KeyUtil.link(UPDATE_ITEM_CACHE_LOCK_KEY, itemId);
    }
}
