package com.harris.app.service.cache;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.harris.app.model.cache.CacheConstant;
import com.harris.app.model.cache.SaleActivityCache;
import com.harris.domain.model.entity.SaleActivity;
import com.harris.domain.service.SaleActivityDomainService;
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
public class SaleActivityCacheService {
    // 锁的 key 的前缀
    private static final String UPDATE_ACTIVITY_CACHE_LOCK_KEY = "UPDATE_ACTIVITY_CACHE_LOCK_KEY_";
    private final Lock localLock = new ReentrantLock();
    // 本地缓存，用于暂存销售活动信息，减少对分布式缓存的访问频率
    private static final Cache<Long, SaleActivityCache> ACTIVITY_LOCAL_CACHE =
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
    private SaleActivityDomainService saleActivityDomainService;
    
    public SaleActivityCache getActivityCache(Long activityId, Long version) {
        if (activityId == null) return null;
        
        // 尝试从本地缓存获取销售活动缓存
        SaleActivityCache saleActivityCache = ACTIVITY_LOCAL_CACHE.getIfPresent(activityId);
        if (saleActivityCache != null) {
            // 获取本地缓存的版本号
            Long localVersion = saleActivityCache.getVersion();
            
            // 如果未提供版本号，或提供的版本号小于等于本地缓存的版本号，则直接返回本地缓存对象
            if (version == null || version <= localVersion) {
                log.info("应用层 getActivityCache，命中本地缓存: [{}]", activityId);
                return saleActivityCache;
            }
        } else {
            log.info("应用层 getActivityCache，未命中本地缓存: [{}]", activityId);
        }
        
        // 如果本地缓存不存在，或者提供的版本号大于本地缓存的版本号，则尝试从远程缓存获取销售活动缓存
        return getLatestDistributedCache(activityId);
    }
    
    private SaleActivityCache getLatestDistributedCache(Long activityId) {
        log.info("应用层 getLatestDistributedCache，读取远程缓存: {}", activityId);
        
        // 尝试从分布式缓存服务获取销售活动缓存对象
        SaleActivityCache distributedActivityCache = distributedCacheService.getObject(buildActivityCacheKey(activityId), SaleActivityCache.class);
        
        // 如果分布式缓存中没有找到，尝试更新分布式缓存
        if (distributedActivityCache == null) distributedActivityCache = tryUpdateActivityCache(activityId);
        
        // 如果获取到的缓存对象有效，且不是标记为稍后再试的对象
        if (distributedActivityCache != null && !distributedActivityCache.isLater()) {
            // 尝试获取本地锁
            boolean lockSuccess = localLock.tryLock();
            if (lockSuccess) {
                try {
                    // 将分布式缓存中的对象更新到本地缓存中
                    ACTIVITY_LOCAL_CACHE.put(activityId, distributedActivityCache);
                    log.info("应用层 getLatestDistributedCache，本地缓存已更新: {}", activityId);
                } finally {
                    localLock.unlock();
                }
            }
        }
        
        // 返回最终的活动缓存对象
        return distributedActivityCache;
    }
    
    public SaleActivityCache tryUpdateActivityCache(Long activityId) {
        log.info("应用层 tryUpdateActivityCache，更新远程缓存: {}", activityId);
        
        // 获取 Redisson 分布式锁，锁的键由 预定义的前缀 + 活动ID 组成
        DistributedLock distributedLock = distributedLockService.getLock(UPDATE_ACTIVITY_CACHE_LOCK_KEY + activityId);
        try {
            // 尝试获取分布式锁
            boolean lockSuccess = distributedLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!lockSuccess) return new SaleActivityCache().tryLater();
            
            // 从域服务中获取活动详情
            SaleActivity saleActivity = saleActivityDomainService.getActivity(activityId);
            
            // 根据获取的活动详情构建活动缓存对象。如果活动存在，填充数据并设置当前时间戳为版本号；
            // 如果活动不存在，设置状态为不存在
            SaleActivityCache saleActivityCache = saleActivity != null
                    ? new SaleActivityCache().with(saleActivity).withVersion(System.currentTimeMillis())
                    : new SaleActivityCache().notExist();
            
            // 将构建的活动缓存对象序列化为JSON字符串，并存入分布式缓存中，设置过期时间为5分钟
            distributedCacheService.put(buildActivityCacheKey(activityId), JSON.toJSONString(saleActivityCache), CacheConstant.MINUTES_5);
            
            log.info("应用层 tryUpdateActivityCache，远程缓存已更新: {}", activityId);
            return saleActivityCache;
        } catch (InterruptedException e) {
            log.error("应用层 tryUpdateActivityCache，远程缓存更新异常: {} ", activityId, e);
            return new SaleActivityCache().tryLater();
        } finally {
            distributedLock.unlock();
        }
    }
    
    
    // 构建活动缓存的 key🇹
    private String buildActivityCacheKey(Long activityId) {
        return KeyUtil.link(CacheConstant.ACTIVITY_CACHE_KEY, activityId);
    }
}
