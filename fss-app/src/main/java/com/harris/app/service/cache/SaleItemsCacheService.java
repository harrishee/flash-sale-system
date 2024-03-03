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
    // 锁的 key 的前缀
    private static final String UPDATE_ITEMS_CACHE_LOCK_KEY = "UPDATE_ITEMS_CACHE_LOCK_KEY_";
    private final Lock localLock = new ReentrantLock();
    
    // 本地缓存，用于暂存销售商品列表信息，减少对分布式缓存的访问频率
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
        if (activityId == null) return null;
        
        // 尝试从本地缓存获取销售商品列表缓存
        SaleItemsCache saleItemsCache = ITEMS_LOCAL_CACHE.getIfPresent(activityId);
        if (saleItemsCache != null) {
            // 如果本地缓存命中，且传入的版本号为null或小于等于缓存中的版本号，则直接返回缓存对象
            if (version == null) {
                log.info("应用层 getItemsCache, 命中本地缓存: {}", activityId);
                return saleItemsCache;
            }
            
            if (version.equals(saleItemsCache.getVersion()) || version < saleItemsCache.getVersion()) {
                log.info("应用层 getItemsCache, 命中本地缓存: [{},{}]", activityId, version);
                return saleItemsCache;
            }
            
            // 如果传入的版本号大于缓存中的版本号，说明本地缓存的数据可能已经过时，需要从分布式缓存中获取最新数据
            log.info("应用层 getItemsCache, 未命中本地缓存: {}", activityId);
            return getLatestDistributedCache(activityId);
        }
        
        // 如果本地缓存未命中，则尝试从分布式缓存中获取最新的缓存数据
        log.info("应用层 getItemsCache, 未命中本地缓存: {}", activityId);
        return getLatestDistributedCache(activityId);
    }
    
    private SaleItemsCache getLatestDistributedCache(Long activityId) {
        log.info("应用层 getLatestDistributedCache, 读取远程缓存: {}", activityId);
        
        // 尝试从分布式缓存服务获取商品列表缓存对象
        SaleItemsCache distributedItemsCache = distributedCacheService.getObject(buildItemCacheKey(activityId), SaleItemsCache.class);
        
        // 如果分布式缓存中没有找到，尝试更新分布式缓存
        if (distributedItemsCache == null) distributedItemsCache = tryUpdateItemsCache(activityId);
        
        // 如果获取到的缓存对象有效，且不是标记为稍后再试的对象
        if (distributedItemsCache != null && !distributedItemsCache.isLater()) {
            // 尝试获取本地锁
            boolean lockSuccess = localLock.tryLock();
            if (lockSuccess) {
                try {
                    // 将分布式缓存中的对象更新到本地缓存中
                    ITEMS_LOCAL_CACHE.put(activityId, distributedItemsCache);
                    log.info("应用层 getLatestDistributedCache, 本地缓存已更新: {}", activityId);
                } finally {
                    localLock.unlock();
                }
            }
        }
        
        // 返回最终的商品列表缓存对象
        return distributedItemsCache;
    }
    
    public SaleItemsCache tryUpdateItemsCache(Long activityId) {
        log.info("应用层 tryUpdateItemsCache, 更新远程缓存: {}", activityId);
        
        // 获取 Redisson 分布式锁，锁的键由 预定义的前缀 和 活动ID 组成
        DistributedLock distributedLock = distributedLockService.getDistributedLock(UPDATE_ITEMS_CACHE_LOCK_KEY + activityId);
        try {
            // 尝试获取分布式锁，设置超时时间为500毫秒，等待时间为1000毫秒
            boolean lockSuccess = distributedLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!lockSuccess) return new SaleItemsCache().tryLater();
            
            // 从域服务中获取销售商品列表信息
            PageQuery pageQuery = new PageQuery().setActivityId(activityId).setStatus(SaleItemStatus.ONLINE.getCode());
            PageResult<SaleItem> itemsPageResult = saleItemDomainService.getItems(pageQuery);
            
            // 根据获取到的销售商品列表信息构建商品列表缓存对象。如果获取的商品列表不为空，则奢姿总数和商品列表数据，否则返回一个空的缓存对象
            SaleItemsCache saleItemsCache = (itemsPageResult != null)
                    ? new SaleItemsCache()
                    .setTotal(itemsPageResult.getTotal())
                    .setSaleItems(itemsPageResult.getData())
                    .setVersion(System.currentTimeMillis())
                    : new SaleItemsCache().empty();
            
            // 将构建的商品缓存对象序列化为JSON字符串，并存入分布式缓存中，设置过期时间为5分钟
            distributedCacheService.put(buildItemCacheKey(activityId), JSON.toJSONString(saleItemsCache), CacheConstant.MINUTES_5);
            
            log.info("应用层 tryUpdateItemsCache, 远程缓存已更新: {}", activityId);
            return saleItemsCache;
        } catch (Exception e) {
            log.error("应用层 tryUpdateItemsCache, 远程缓存更新异常: {} ", activityId, e);
            return new SaleItemsCache().tryLater();
        } finally {
            distributedLock.unlock();
        }
    }
    
    // 构建商品列表缓存的 key
    private String buildItemCacheKey(Long activityId) {
        return KeyUtil.link(CacheConstant.ITEMS_CACHE_KEY + activityId);
    }
}
