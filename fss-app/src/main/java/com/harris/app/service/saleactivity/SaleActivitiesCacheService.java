package com.harris.app.service.saleactivity;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.harris.app.model.cache.CacheConstant;
import com.harris.app.model.cache.SaleActivitiesCache;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.PageResult;
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
public class SaleActivitiesCacheService {
    private static final String UPDATE_ACTIVITIES_CACHE_LOCK_KEY = "UPDATE_ACTIVITIES_CACHE_LOCK_KEY";
    private final Lock localLock = new ReentrantLock();
    private static final Cache<Integer, SaleActivitiesCache> ACTIVITIES_LOCAL_CACHE =
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
    
    public SaleActivitiesCache getActivitiesCache(Integer pageNumber, Long version) {
        if (pageNumber == null) pageNumber = 1;
        
        SaleActivitiesCache saleActivitiesCache = ACTIVITIES_LOCAL_CACHE.getIfPresent(pageNumber);
        if (saleActivitiesCache != null) {
            if (version == null) {
                // log.info("获取活动列表缓存，命中本地缓存，无版本号: [pageNumber: {}]", pageNumber);
                return saleActivitiesCache;
            }
            if (version.equals(saleActivitiesCache.getVersion()) || version < saleActivitiesCache.getVersion()) {
                // log.info("获取活动列表缓存，命中本地缓存，有版本号: [pageNumber: {}, version: {}]", pageNumber, version);
                return saleActivitiesCache;
            }
            
            // log.info("获取活动列表缓存，命中本地缓存，但本地缓存非最新: [activityId: {}, version: {}]", activityId, version);
            return getDistributedCache(pageNumber);
        }
        
        // log.info("获取活动列表缓存，未命中本地缓存: [activityId: {}]", activityId);
        return getDistributedCache(pageNumber);
    }
    
    private SaleActivitiesCache getDistributedCache(Integer pageNumber) {
        // 1. 从分布式缓存中获取活动列表缓存，key = ACTIVITIES_CACHE_KEY + pageNumber
        SaleActivitiesCache redisActivitiesCache = distributedCacheService.get(buildActivityCacheKey(pageNumber), SaleActivitiesCache.class);
        
        // 2. 分布式缓存中也没有，说明 缓存数据尚未生成 或者 缓存失效，尝试更新缓存
        if (redisActivitiesCache == null) redisActivitiesCache = tryUpdateDistActivitiesCache(pageNumber);
        
        // 3. 分布式缓存中有缓存，并且未处于更新中状态，说明缓存是最新的，可用；将其更新到本地缓存中
        if (redisActivitiesCache != null && !redisActivitiesCache.isLater()) {
            // 4. 同一时间只允许一个线程更新本地缓存
            boolean lockSuccess = localLock.tryLock();
            if (lockSuccess) {
                try {
                    ACTIVITIES_LOCAL_CACHE.put(pageNumber, redisActivitiesCache);
                } finally {
                    localLock.unlock();
                }
            }
        }
        
        // 5. 最后返回的：最新状态缓存 / 稍后再试（tryLater） / 不存在（notExist）
        return redisActivitiesCache;
    }
    
    public SaleActivitiesCache tryUpdateDistActivitiesCache(Integer pageNumber) {
        // 获取分布式锁实例，防止并发更新活动列表缓存，key = UPDATE_ACTIVITIES_CACHE_LOCK_KEY + pageNumber
        DistributedLock rLock = distributedLockService.getLock(buildUpdateActivityCacheKey(pageNumber));
        try {
            boolean lockSuccess = rLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!lockSuccess) return new SaleActivitiesCache().tryLater();
            
            // 从领域服层调用 获取活动列表 方法
            PageQuery pageQuery = new PageQuery().setPageNumber(pageNumber);
            PageResult<SaleActivity> activitiesPageResult = saleActivityDomainService.getActivities(pageQuery);
            
            // 如果活动列表不存在，设置状态为不存在；如果存在，设置数据和添加当前时间戳为版本号
            SaleActivitiesCache saleActivitiesCache;
            if (activitiesPageResult == null) {
                saleActivitiesCache = new SaleActivitiesCache().notExist();
            } else {
                saleActivitiesCache = new SaleActivitiesCache()
                        .setTotal(activitiesPageResult.getTotal())
                        .setSaleActivities(activitiesPageResult.getData())
                        .setVersion(System.currentTimeMillis());
            }
            
            // 更新分布式缓存，key = ACTIVITIES_CACHE_KEY + pageNumber
            distributedCacheService.put(buildActivityCacheKey(pageNumber), JSON.toJSONString(saleActivitiesCache), CacheConstant.MINUTES_5);
            
            // log.info("分布式活动缓存已更新: [saleActivitiesCache={}]", saleActivitiesCache);
            return saleActivitiesCache;
        } catch (InterruptedException e) {
            log.error("更新分布式活动缓存异常: [pageNumber: {}] ", pageNumber, e);
            return new SaleActivitiesCache().tryLater();
        } finally {
            rLock.unlock();
        }
    }
    
    private String buildActivityCacheKey(Integer pageNumber) {
        return KeyUtil.link(CacheConstant.ACTIVITIES_CACHE_KEY, pageNumber);
    }
    
    private String buildUpdateActivityCacheKey(Integer pageNumber) {
        return KeyUtil.link(UPDATE_ACTIVITIES_CACHE_LOCK_KEY, pageNumber);
    }
}
