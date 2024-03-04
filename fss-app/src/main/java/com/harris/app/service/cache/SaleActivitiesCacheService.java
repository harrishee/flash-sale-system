package com.harris.app.service.cache;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.harris.app.model.cache.CacheConstant;
import com.harris.app.model.cache.SaleActivitiesCache;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.PageResult;
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
public class SaleActivitiesCacheService {
    // 锁的 key 的前缀
    private static final String UPDATE_ACTIVITIES_CACHE_LOCK_KEY = "UPDATE_ACTIVITIES_CACHE_LOCK_KEY";
    private final Lock localLock = new ReentrantLock();
    
    
    // 本地缓存，用于暂存销售活动列表信息，减少对分布式缓存的访问频率
    private static final Cache<Integer, SaleActivitiesCache> activitiesLocalCache =
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
        pageNumber = pageNumber == null ? 1 : pageNumber;
        
        // 尝试从本地缓存获取销售活动列表缓存
        SaleActivitiesCache saleActivitiesCache = activitiesLocalCache.getIfPresent(pageNumber);
        if (saleActivitiesCache != null) {
            // 获取本地缓存的版本号
            Long localVersion = saleActivitiesCache.getVersion();
            
            // 如果未提供版本号，或提供的版本号小于等于本地缓存的版本号，则直接返回本地缓存对象
            if (version == null || version <= localVersion) {
                log.info("应用层 getActivitiesCache, 命中本地缓存: {}", pageNumber);
                return saleActivitiesCache;
            }
        } else {
            log.info("应用层 getActivitiesCache, 未命中本地缓存: {}", pageNumber);
        }
        
        // 如果本地缓存不存在，或者提供的版本号大于本地缓存的版本号，则尝试从远程缓存获取销售活动列表缓存
        return getLatestDistributedCache(pageNumber);
    }
    
    private SaleActivitiesCache getLatestDistributedCache(Integer pageNumber) {
        log.info("应用层 getLatestDistributedCache, 读取远程缓存: {}", pageNumber);
        
        // 尝试从分布式缓存服务获取销售活动列表缓存对象
        SaleActivitiesCache distributedActivitiesCache = distributedCacheService.getObject(buildActivityCacheKey(pageNumber), SaleActivitiesCache.class);
        
        // 如果分布式缓存中没有找到，尝试更新分布式缓存
        if (distributedActivitiesCache == null) distributedActivitiesCache = tryUpdateActivitiesCache(pageNumber);
        
        // 如果获取到的缓存对象有效，且不是标记为稍后再试的对象
        if (distributedActivitiesCache != null && !distributedActivitiesCache.isLater()) {
            // 尝试获取本地锁
            boolean lockSuccess = localLock.tryLock();
            if (lockSuccess) {
                try {
                    // 将分布式缓存中的对象更新到本地缓存中
                    activitiesLocalCache.put(pageNumber, distributedActivitiesCache);
                    log.info("应用层 getLatestDistributedCache, 本地缓存已更新: {}", pageNumber);
                } finally {
                    localLock.unlock();
                }
            }
        }
        
        return distributedActivitiesCache;
    }
    
    public SaleActivitiesCache tryUpdateActivitiesCache(Integer pageNumber) {
        log.info("应用层 tryUpdateActivitiesCache, 更新远程缓存: {}", pageNumber);
        
        // 获取 Redisson 分布式锁，锁的键由 预定义的前缀 + 页码 组成
        DistributedLock distributedLock = distributedLockService.getLock(UPDATE_ACTIVITIES_CACHE_LOCK_KEY + pageNumber);
        try {
            // 尝试获取分布式锁
            boolean lockSuccess = distributedLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!lockSuccess) return new SaleActivitiesCache().tryLater();
            
            // 从域服务中获取活动列表详情
            PageQuery pageQuery = new PageQuery().setPageNumber(pageNumber);
            PageResult<SaleActivity> activitiesPageResult = saleActivityDomainService.getActivities(pageQuery);
            
            // 根据获取的活动列表详情构建活动列表缓存对象。如果获取的活动列表不为空，则设置总数和活动列表；否则设置不存在标记
            SaleActivitiesCache saleActivitiesCache;
            if (activitiesPageResult == null) {
                saleActivitiesCache = new SaleActivitiesCache().notExist();
            } else {
                saleActivitiesCache = new SaleActivitiesCache()
                        .setTotal(activitiesPageResult.getTotal())
                        .setSaleActivities(activitiesPageResult.getData())
                        .setVersion(System.currentTimeMillis());
            }
            
            // 将构建的活动缓存对象序列化为JSON字符串，并存入分布式缓存中，设置过期时间为5分钟
            distributedCacheService.put(buildActivityCacheKey(pageNumber), JSON.toJSONString(saleActivitiesCache), CacheConstant.MINUTES_5);
            
            log.info("应用层 tryUpdateActivitiesCache, 远程缓存已更新: {}", pageNumber);
            return saleActivitiesCache;
        } catch (InterruptedException e) {
            log.error("应用层 tryUpdateActivitiesCache, 远程缓存更新异常: {} ", pageNumber, e);
            return new SaleActivitiesCache().tryLater();
        } finally {
            distributedLock.unlock();
        }
    }
    
    // 构建活动列表缓存的 key
    private String buildActivityCacheKey(Integer pageNumber) {
        return KeyUtil.link(CacheConstant.ACTIVITIES_CACHE_KEY, pageNumber);
    }
}
