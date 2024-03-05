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
    private static final String UPDATE_ACTIVITY_CACHE_LOCK_KEY = "UPDATE_ACTIVITY_CACHE_LOCK_KEY";
    private final Lock localLock = new ReentrantLock();
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
        
        SaleActivityCache saleActivityCache = ACTIVITY_LOCAL_CACHE.getIfPresent(activityId);
        if (saleActivityCache != null) {
            // 如果本地缓存命中，且传入的版本号为null或小于等于缓存中的版本号，则直接返回缓存对象
            if (version == null || version.equals(saleActivityCache.getVersion()) || version < saleActivityCache.getVersion()) {
                // log.info("应用层 getActivityCache，命中本地缓存: [activityId: {}]", activityId);
                return saleActivityCache;
            }
            // log.info("应用层 getActivityCache，未命中本地缓存: [activityId: {}, version: {}]", activityId, version);
            return getDistributedCache(activityId);
        }
        
        // 如果本地缓存不存在，或者提供的版本号大于本地缓存的版本号，则尝试从远程缓存获取销售活动缓存
        // log.info("应用层 getActivityCache，未命中本地缓存: [activityId: {}]", activityId);
        return getDistributedCache(activityId);
    }
    
    private SaleActivityCache getDistributedCache(Long activityId) {
        // 尝试从分布式缓存服务获取销售活动缓存对象，key = ACTIVITY_CACHE_KEY + activityId
        SaleActivityCache distributedActivityCache = distributedCacheService.get(buildActivityCacheKey(activityId), SaleActivityCache.class);
        // 如果分布式缓存中没有找到，说明是第一次获取，或者缓存已过期，尝试更新缓存
        if (distributedActivityCache == null) distributedActivityCache = tryUpdateActivityCache(activityId);
        
        // 如果获取到的缓存对象有效，且不是标记为稍后再试的对象
        if (distributedActivityCache != null && !distributedActivityCache.isLater()) {
            boolean lockSuccess = localLock.tryLock();
            if (lockSuccess) {
                try {
                    // 将分布式缓存中的对象更新到本地缓存中
                    ACTIVITY_LOCAL_CACHE.put(activityId, distributedActivityCache);
                } finally {
                    localLock.unlock();
                }
            }
        }
        
        return distributedActivityCache;
    }
    
    public SaleActivityCache tryUpdateActivityCache(Long activityId) {
        // 获取 Redisson 分布式锁，防止并发更新活动缓存，key = UPDATE_ACTIVITY_CACHE_LOCK_KEY + activityId
        DistributedLock rLock = distributedLockService.getLock(buildUpdateActivityCacheKey(activityId));
        try {
            // 尝试获取分布式锁
            boolean lockSuccess = rLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!lockSuccess) {
                log.info("未获取到并发锁：[activityId={}]", activityId);
                return new SaleActivityCache().tryLater();
            }
            
            // 从域服务中获取活动详情
            SaleActivity saleActivity = saleActivityDomainService.getActivity(activityId);
            
            // 根据获取的活动详情构建活动缓存对象。如果活动存在，填充数据并设置当前时间戳为版本号；
            // 如果活动不存在，设置状态为不存在
            SaleActivityCache saleActivityCache = saleActivity != null
                    ? new SaleActivityCache().with(saleActivity).withVersion(System.currentTimeMillis())
                    : new SaleActivityCache().notExist();
            
            // 将构建的活动缓存对象序列化为JSON字符串，并存入分布式缓存中，设置过期时间为5分钟
            distributedCacheService.put(buildActivityCacheKey(activityId), JSON.toJSONString(saleActivityCache), CacheConstant.MINUTES_5);
            
            // log.info("应用层 tryUpdateActivityCache，远程缓存已更新: [activityId: {}]", activityId);
            return saleActivityCache;
        } catch (InterruptedException e) {
            log.error("应用层 tryUpdateActivityCache，远程缓存更新异常: [activityId: {}] ", activityId, e);
            return new SaleActivityCache().tryLater();
        } finally {
            rLock.unlock();
        }
    }
    
    private String buildActivityCacheKey(Long activityId) {
        return KeyUtil.link(CacheConstant.ACTIVITY_CACHE_KEY, activityId);
    }
    
    private String buildUpdateActivityCacheKey(Long activityId) {
        return KeyUtil.link(UPDATE_ACTIVITY_CACHE_LOCK_KEY, activityId);
    }
}
