package com.harris.app.service.saleactivity;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.harris.app.model.cache.CacheConstant;
import com.harris.app.model.cache.SaleActivityCache;
import com.harris.domain.model.entity.SaleActivity;
import com.harris.domain.service.activity.SaleActivityDomainService;
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
        SaleActivityCache saleActivityCache = ACTIVITY_LOCAL_CACHE.getIfPresent(activityId);
        
        // 1.1 如果本地有缓存，进一步判断版本号，否则直接从分布式缓存获取
        if (saleActivityCache != null) {
            // 2.1 无版本号情况下直接返回本地缓存
            if (version == null) {
                // log.info("获取活动缓存，命中本地缓存，无版本号: [activityId: {}]", activityId);
                return saleActivityCache;
            } else if (version.equals(saleActivityCache.getVersion()) || version < saleActivityCache.getVersion()) {
                // 2.2 有版本号情况下，版本号一致 或者 本地缓存版本号大于提供版本号，说明本地缓存是最新的
                // log.info("获取活动缓存，命中本地缓存，有版本号: [activityId: {}, version: {}]", activityId, version);
                return saleActivityCache;
            } else {
                // 2.3 本地缓存版本号小于提供的版本号，说明本地缓存不是最新的，从分布式缓存获取
                // log.info("获取活动缓存，命中本地缓存，但本地缓存非最新: [activityId: {}, version: {}]", activityId, version);
                return getDistributedCache(activityId);
            }
        }
        
        // 1.2 本地没有缓存，直接从分布式缓存获取
        // log.info("获取活动缓存，未命中本地缓存: [activityId: {}]", activityId);
        return getDistributedCache(activityId);
    }
    
    private SaleActivityCache getDistributedCache(Long activityId) {
        // 1. 从分布式缓存中获取活动缓存，key = ACTIVITY_CACHE_KEY + activityId
        SaleActivityCache redisActivityCache = distributedCacheService.get(buildActivityCacheKey(activityId), SaleActivityCache.class);
        
        // 2. 分布式缓存中也没有，说明 缓存数据尚未生成 或者 缓存失效，尝试更新缓存
        if (redisActivityCache == null) redisActivityCache = tryUpdateDistActivityCache(activityId);
        
        // 3. 分布式缓存中有缓存，并且未处于更新中状态，说明缓存是最新的，可用；将其更新到本地缓存中
        if (redisActivityCache != null && !redisActivityCache.isLater()) {
            // 4. 同一时间只允许一个线程更新本地缓存
            boolean lockSuccess = localLock.tryLock();
            if (lockSuccess) {
                try {
                    ACTIVITY_LOCAL_CACHE.put(activityId, redisActivityCache);
                } finally {
                    localLock.unlock();
                }
            }
        }
        
        // 5. 最后返回的：最新状态缓存 / 稍后再试（tryLater） / 不存在（notExist）
        return redisActivityCache;
    }
    
    public SaleActivityCache tryUpdateDistActivityCache(Long activityId) {
        // 获取分布式锁实例，防止并发更新活动缓存，key = UPDATE_ACTIVITY_CACHE_LOCK_KEY + activityId
        DistributedLock rLock = distributedLockService.getLock(buildUpdateActivityCacheKey(activityId));
        try {
            boolean lockSuccess = rLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!lockSuccess) return new SaleActivityCache().tryLater();
            
            // 从领域服层调用 获取活动 方法
            SaleActivity saleActivity = saleActivityDomainService.getActivity(activityId);
            
            // 如果活动不存在，设置状态为不存在；如果存在，设置数据和添加当前时间戳为版本号
            SaleActivityCache saleActivityCache;
            if (saleActivity == null) {
                saleActivityCache = new SaleActivityCache().notExist();
            } else {
                saleActivityCache = new SaleActivityCache().with(saleActivity).withVersion(System.currentTimeMillis());
            }
            
            // 更新活动的分布式缓存，key = ACTIVITY_CACHE_KEY + activityId
            distributedCacheService.put(buildActivityCacheKey(activityId), JSON.toJSONString(saleActivityCache), CacheConstant.MINUTES_5);
            
            // log.info("分布式活动缓存已更新: [saleActivityCache={}]", saleActivityCache);
            return saleActivityCache;
        } catch (InterruptedException e) {
            log.error("更新分布式活动缓存异常: [activityId: {}] ", activityId, e);
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
