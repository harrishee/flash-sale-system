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
    // 锁的 key 的前缀
    private static final String UPDATE_ITEM_CACHE_LOCK_KEY = "UPDATE_ITEM_CACHE_LOCK_KEY_";
    private final Lock localLock = new ReentrantLock();
    
    // 本地缓存，用于暂存销售商品信息，减少对分布式缓存的访问频率
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
        if (itemId == null) return null;
        
        // 尝试从本地缓存获取销售商品缓存
        SaleItemCache saleItemCache = ITEM_LOCAL_CACHE.getIfPresent(itemId);
        if (saleItemCache != null) {
            // 如果本地缓存命中，且传入的版本号为null或小于等于缓存中的版本号，则直接返回缓存对象
            if (version == null) {
                log.info("应用层 getItemCache, 命中本地缓存: [{}]", itemId);
                return saleItemCache;
            }
            if (version.equals(saleItemCache.getVersion()) || version < saleItemCache.getVersion()) {
                log.info("应用层 getItemCache, 命中本地缓存: [{},{}]", itemId, version);
                return saleItemCache;
            }
            
            // 如果传入的版本号大于缓存中的版本号，说明本地缓存的数据可能已经过时，需要从分布式缓存中获取最新数据
            log.info("应用层 getItemCache, 未命中本地缓存: [{},{}]", itemId, version);
            return getLatestDistributedCache(itemId);
        }
        
        // 如果本地缓存未命中，则尝试从分布式缓存中获取最新的缓存数据
        log.info("应用层 getItemCache, 未命中本地缓存: [{},{}]", itemId, version);
        return getLatestDistributedCache(itemId);
    }
    
    private SaleItemCache getLatestDistributedCache(Long itemId) {
        log.info("应用层 getLatestDistributedCache, 读取远程缓存: [{}]", itemId);
        
        // 尝试从分布式缓存服务获取商品缓存对象
        SaleItemCache distributedItemCache = distributedCacheService.getObject(buildItemCacheKey(itemId), SaleItemCache.class);
        
        // 如果分布式缓存中没有找到，尝试更新分布式缓存
        if (distributedItemCache == null) distributedItemCache = tryUpdateItemCache(itemId);
        
        // 如果获取到的缓存对象有效，且不是标记为稍后再试的对象
        if (distributedItemCache != null && !distributedItemCache.isLater()) {
            // 尝试获取本地锁
            boolean lockSuccess = localLock.tryLock();
            if (lockSuccess) {
                try {
                    // 将分布式缓存中的对象更新到本地缓存中
                    ITEM_LOCAL_CACHE.put(itemId, distributedItemCache);
                    log.info("应用层 getLatestDistributedCache, 本地缓存已更新: [{}]", itemId);
                } finally {
                    localLock.unlock();
                }
            }
        }
        
        // 返回最终的商品缓存对象
        return distributedItemCache;
    }
    
    public SaleItemCache tryUpdateItemCache(Long itemId) {
        log.info("应用层 tryUpdateItemCache, 更新远程缓存: [{}]", itemId);
        
        // 获取 Redisson 分布式锁，锁的键由 预定义的前缀 和 商品ID 拼接而成
        DistributedLock distributedLock = distributedLockService.getLock(UPDATE_ITEM_CACHE_LOCK_KEY + itemId);
        try {
            // 尝试获取分布式锁
            boolean lockSuccess = distributedLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!lockSuccess) return new SaleItemCache().tryLater();
            
            // 尝试从分布式缓存服务中获取商品缓存对象，如果已经存在则直接返回，不用再次更新
            SaleItemCache distributedItemCache = distributedCacheService.getObject(buildItemCacheKey(itemId), SaleItemCache.class);
            if (distributedItemCache != null) return distributedItemCache;
            
            // 从领域服务中获取商品信息
            SaleItem saleItem = saleItemDomainService.getItem(itemId);
            
            // 根据获取的商品信息构建商品缓存对象。如果商品存在，填充数据并设置当前时间戳为版本号；
            // 如果商品不存在，设置缓存对象为不存在状态
            SaleItemCache saleItemCache = (saleItem != null)
                    ? new SaleItemCache().with(saleItem).withVersion(System.currentTimeMillis())
                    : new SaleItemCache().notExist();
            
            // 将构建的活动缓存对象序列化为JSON字符串，并存入分布式缓存中，设置过期时间为5分钟
            distributedCacheService.put(buildItemCacheKey(itemId), JSON.toJSONString(saleItemCache), CacheConstant.MINUTES_5);
            
            log.info("应用层 tryUpdateItemCache, 远程缓存已更新: [{}]", itemId);
            return saleItemCache;
        } catch (InterruptedException e) {
            log.error("应用层 tryUpdateItemCache, 远程缓存更新异常: [{}]", itemId, e);
            return new SaleItemCache().tryLater();
        } finally {
            distributedLock.unlock();
        }
    }
    
    // 构建商品缓存的键
    private String buildItemCacheKey(Long itemId) {
        return KeyUtil.link(CacheConstant.ITEM_CACHE_KEY, itemId);
    }
}
