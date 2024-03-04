package com.harris.app.service.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.harris.app.model.cache.StockCache;
import com.harris.app.util.PlaceOrderCondition;
import com.harris.domain.model.StockDeduction;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.service.SaleItemDomainService;
import com.harris.infra.cache.DistributedCacheService;
import com.harris.infra.cache.RedisCacheService;
import com.harris.infra.util.KeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Conditional(PlaceOrderCondition.class)
public class StandardStockCacheService implements StockCacheService {
    // 锁的 key 的前缀
    private static final int IN_STOCK_ALIGNING = -9; // 库存对齐过程中的标志
    private static final String STOCK_CACHE_KEY = "STOCK_CACHE_KEY";
    private static final String STOCK_ALIGN_LOCK_KEY = "STOCK_ALIGN_LOCK_KEY";
    private static final String INIT_OR_ALIGN_STOCK_LUA;
    private static final String REVERT_STOCK_LUA;
    private static final String DEDUCT_STOCK_LUA;
    
    // 本地缓存的初始化，用于减少对外部系统的访问
    private static final Cache<Long, StockCache> STOCK_LOCAL_CACHE =
            CacheBuilder.newBuilder()
                    .initialCapacity(10)
                    .concurrencyLevel(5)
                    .expireAfterWrite(10, TimeUnit.SECONDS)
                    .build();
    
    static {
        // 用于 预热 或 对齐 库存
        INIT_OR_ALIGN_STOCK_LUA = "if (redis.call('exists', KEYS[2]) == 1) then" +
                "    return -997;" +
                "end;" +
                "redis.call('set', KEYS[2] , 1);" +
                "local stockNumber = tonumber(ARGV[1]);" +
                "redis.call('set', KEYS[1] , stockNumber);" +
                "redis.call('del', KEYS[2]);" +
                "return 1";
        
        // 用于回滚库存
        REVERT_STOCK_LUA = "if (redis.call('exists', KEYS[2]) == 1) then" +
                "    return -9;" +
                "end;" +
                "if (redis.call('exists', KEYS[1]) == 1) then" +
                "    local stock = tonumber(redis.call('get', KEYS[1]));" +
                "    local num = tonumber(ARGV[1]);" +
                "    redis.call('incrby', KEYS[1] , num);" +
                "    return 1;" +
                "end;" +
                "return -1;";
        
        // 用于扣减库存
        DEDUCT_STOCK_LUA = "if (redis.call('exists', KEYS[2]) == 1) then" +
                "    return -9;" +
                "end;" +
                "if (redis.call('exists', KEYS[1]) == 1) then" +
                "    local stock = tonumber(redis.call('get', KEYS[1]));" +
                "    local num = tonumber(ARGV[1]);" +
                "    if (stock < num) then" +
                "        return -3" +
                "    end;" +
                "    if (stock >= num) then" +
                "        redis.call('incrby', KEYS[1], 0 - num);" +
                "        return 1" +
                "    end;" +
                "    return -2;" +
                "end;" +
                "return -1;";
    }
    
    @Resource
    private RedisCacheService redisCacheService;
    
    @Resource
    private DistributedCacheService distributedCacheService;
    
    @Resource
    private SaleItemDomainService saleItemDomainService;
    
    @Override
    public StockCache getStockCache(Long userId, Long itemId) {
        // 从本地缓存中获取库存缓存对象，如果存在则直接返回
        StockCache stockCache = STOCK_LOCAL_CACHE.getIfPresent(itemId);
        if (stockCache != null) return stockCache;
        
        System.out.println("key: " + buildStockCacheKey(itemId));
        // 否则，从分布式缓存中获取可用库存数量，还不存在则返回 null
        Integer availableStockQuantity = distributedCacheService.getObject(buildStockCacheKey(itemId), Integer.class);
        
        System.out.println("availableStockQuantity: " + availableStockQuantity);
        
        if (availableStockQuantity == null) return null;
        
        // 本地不存在，分布式存在，创建新的库存缓存对象并存入本地缓存
        stockCache = new StockCache().with(availableStockQuantity);
        STOCK_LOCAL_CACHE.put(itemId, stockCache);
        return stockCache;
    }
    
    @Override
    public boolean syncCachedStockToDB(Long itemId) {
        if (itemId == null) {
            log.info("应用层 syncCachedStockToDB, 参数 itemId 为空");
            return false;
        }
        
        try {
            // 从领域服务获取销售项对象
            SaleItem saleItem = saleItemDomainService.getItem(itemId);
            if (saleItem == null) {
                log.info("应用层 syncCachedStockToDB, 商品不存在: [itemId={}]", itemId);
                return false;
            }
            if (saleItem.getInitialStock() == null) {
                log.info("应用层 syncCachedStockToDB, 商品库存未设置: [itemId={}]", itemId);
                return false;
            }
            
            // 构建库存缓存键和库存对齐锁键
            String stockCacheKey = buildStockCacheKey(itemId);
            String stockAlignKey = buildStockAlignKey(itemId);
            
            // 执行 Lua 脚本进行库存对齐
            List<String> keys = Lists.newArrayList(stockCacheKey, stockAlignKey);
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(INIT_OR_ALIGN_STOCK_LUA, Long.class);
            Long result = redisCacheService.getRedisTemplate().execute(redisScript, keys, saleItem.getAvailableStock());
            
            if (result == null) {
                log.info("应用层 syncCachedStockToDB，商品库存校准失败: [itemId={}, availableStock={}]", itemId, saleItem.getAvailableStock());
                return false;
            }
            if (result == -997) {
                log.info("应用层 syncCachedStockToDB，已在校准中，本次校准取消: [itemId={}, availableStock={}]", itemId, saleItem.getAvailableStock());
                return true;
            }
            if (result == 1) {
                // log.info("应用层 syncCachedStockToDB，商品库存校准成功: [itemId={}, availableStock={}]", itemId, saleItem.getAvailableStock());
                return true;
            }
            
            // 其他情况返回失败
            return false;
        } catch (Exception e) {
            log.error("应用层 syncCachedStockToDB, 商品库存校准异常: [itemId={}]", itemId, e);
            return false;
        }
    }
    
    @Override
    public boolean deductStock(StockDeduction stockDeduction) {
        log.info("应用层 deductStock，申请库存预扣减: [{}]", stockDeduction);
        if (stockDeduction == null || stockDeduction.invalidParams()) return false;
        
        try {
            // 构建库存缓存键和库存对齐锁键
            String stockCacheKey = buildStockCacheKey(stockDeduction.getItemId());
            String stockAlignKey = buildStockAlignKey(stockDeduction.getItemId());
            
            // 准备Lua脚本执行所需的键列表，包含库存缓存键和库存对齐锁键
            List<String> keys = Lists.newArrayList(stockCacheKey, stockAlignKey);
            
            // 创建Lua脚本执行器，指定脚本和返回类型
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(DEDUCT_STOCK_LUA, Long.class);
            Long result = null;
            long startTime = System.currentTimeMillis();
            
            // 循环执行Lua脚本，直到成功或超过1500毫秒的超时时间
            while ((result == null || result == IN_STOCK_ALIGNING) && (System.currentTimeMillis() - startTime) < 1500) {
                // 执行Lua脚本，传递键和扣减数量作为参数
                result = redisCacheService.getRedisTemplate().execute(redisScript, keys, stockDeduction.getQuantity());
                
                // 库存校准失败
                if (result == null || result == -1 || result == -2 || result == -3) {
                    log.info("应用层 deductStock, 库存预扣减失败: [{}]", stockCacheKey);
                    return false;
                }
                
                // 库存正在对齐中，等待20毫秒后重试
                if (result == IN_STOCK_ALIGNING) {
                    log.info("应用层 deductStock, 库存校准中: [{}]", stockCacheKey);
                    Thread.sleep(20);
                }
                
                // 库存扣减成功
                if (result == 1) {
                    log.info("应用层 deductStock, 库存预扣减成功: [{}]", stockCacheKey);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("应用层 deductStock, 库存预扣减异常: ", e);
            return false;
        }
        
        // 如果超过重试时间仍未成功，返回失败
        return false;
    }
    
    @Override
    public boolean revertStock(StockDeduction stockDeduction) {
        log.info("应用层 revertStock，申请库存预回滚: [{}]", stockDeduction);
        if (stockDeduction == null || stockDeduction.invalidParams()) return false;
        
        try {
            // 构建库存缓存键和库存对齐锁键
            String stockCacheKey = buildStockCacheKey(stockDeduction.getItemId());
            String stockAlignKey = buildStockAlignKey(stockDeduction.getItemId());
            
            // 准备Lua脚本执行所需的键列表，包含库存缓存键和库存对齐锁键
            List<String> keys = Lists.newArrayList(stockCacheKey, stockAlignKey);
            
            // 创建Lua脚本执行器，指定脚本和返回类型
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(REVERT_STOCK_LUA, Long.class);
            Long result = null;
            long startTime = System.currentTimeMillis();
            
            // 循环执行Lua脚本，直到成功或超过1500毫秒的超时时间
            while ((result == null || result == IN_STOCK_ALIGNING) && (System.currentTimeMillis() - startTime) < 1500) {
                // 执行Lua脚本，传递键和回滚数量作为参数
                result = redisCacheService.getRedisTemplate().execute(redisScript, keys, stockDeduction.getQuantity());
                
                // 库存校准失败
                if (result == null || result == -1) {
                    log.info("应用层 revertStock, 库存回滚失败: [{}]", stockCacheKey);
                    return false;
                }
                
                // 库存正在对齐中，等待20毫秒后重试
                if (result == IN_STOCK_ALIGNING) {
                    log.info("应用层 revertStock, 库存校准中: [{}]", stockCacheKey);
                    Thread.sleep(20);
                }
                
                // 库存回滚成功
                if (result == 1) {
                    log.info("应用层 revertStock, 库存回滚成功: [{}]", stockCacheKey);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("应用层 revertStock, 库存回滚异常: ", e);
            return false;
        }
        
        // 如果超过重试时间仍未成功，返回失败
        return false;
    }
    
    // 构建库存对齐锁键
    public static String buildStockAlignKey(Long itemId) {
        return KeyUtil.link(STOCK_ALIGN_LOCK_KEY, itemId);
    }
    
    // 构建库存缓存键
    public static String buildStockCacheKey(Long itemId) {
        return KeyUtil.link(STOCK_CACHE_KEY, itemId);
    }
}
