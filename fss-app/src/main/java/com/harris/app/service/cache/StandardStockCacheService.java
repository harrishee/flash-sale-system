package com.harris.app.service.cache;

import com.alibaba.fastjson.JSON;
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
import com.harris.infra.util.LinkUtil;
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
    private static final int IN_STOCK_ALIGNING = -9;
    private static final String STOCK_CACHE_KEY = "STOCK_CACHE_KEY";
    private static final String STOCK_ALIGN_LOCK_KEY = "STOCK_ALIGN_LOCK_KEY";
    private static final String INIT_OR_ALIGN_STOCK_LUA;
    private static final String REVERT_STOCK_LUA;
    private static final String DEDUCT_STOCK_LUA;
    private final static Cache<Long, StockCache> stockLocalCache =
            CacheBuilder.newBuilder()
                    .initialCapacity(10)
                    .concurrencyLevel(5)
                    .expireAfterWrite(10, TimeUnit.SECONDS)
                    .build();

    static {
        INIT_OR_ALIGN_STOCK_LUA = "if (redis.call('exists', KEYS[2]) == 1) then" +
                "    return -997;" +
                "end;" +
                "redis.call('set', KEYS[2] , 1);" +
                "local stockNumber = tonumber(ARGV[1]);" +
                "redis.call('set', KEYS[1] , stockNumber);" +
                "redis.call('del', KEYS[2]);" +
                "return 1";

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
        // Get from local cache first
        StockCache stockCache = stockLocalCache.getIfPresent(itemId);
        if (stockCache != null) {
            return stockCache;
        }

        // Get from distributed cache if not in local cache
        Integer availableStockQuantity = distributedCacheService.getObject(buildStockCacheKey(itemId), Integer.class);
        if (availableStockQuantity == null) {
            return null;
        }

        // Create a new stock cache with available stock quantity and update the local cache
        stockCache = new StockCache().with(availableStockQuantity);
        stockLocalCache.put(itemId, stockCache);
        return stockCache;
    }

    @Override
    public boolean alignStock(Long itemId) {
        if (itemId == null) {
            log.info("alignStock, no itemId");
            return false;
        }

        try {
            // Retrieve the sale item and check if it exists
            SaleItem saleItem = saleItemDomainService.getItem(itemId);
            if (saleItem == null) {
                log.info("alignStock, item not exist: {}", itemId);
                return false;
            }

            // Check if the initial stock for the item is set up
            if (saleItem.getInitialStock() == null) {
                log.info("alignStock, stock not set up: {}", itemId);
                return false;
            }

            // Build keys for the Redis cache
            String stockCacheKey = buildStockCacheKey(itemId);
            String stockAlignKey = buildStockAlignKey(itemId);
            List<String> keys = Lists.newArrayList(stockCacheKey, stockAlignKey);

            // Prepare and execute the Redis LUA script for stock alignment
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(INIT_OR_ALIGN_STOCK_LUA, Long.class);
            Long result = redisCacheService.getRedisTemplate().execute(redisScript, keys, saleItem.getAvailableStock());
            if (result == null) {
                log.info("alignStock, align failed: {},{},{}", itemId, stockCacheKey, saleItem.getInitialStock());
                return false;
            }

            // Check if stock alignment is already in process
            if (result == -997) {
                log.info("alignStock in process, align canceled: {},{},{},{}", result, itemId,
                        stockCacheKey, saleItem.getInitialStock());
                return true;
            }

            // Successful stock alignment
            if (result == 1) {
                log.info("alignStock, align success: {},{},{},{}", result, itemId, stockCacheKey,
                        saleItem.getInitialStock());
                return true;
            }

            // Default case, return false for unhandled results
            return false;
        } catch (Exception e) {
            log.error("alignStock error: {}", itemId, e);
            return false;
        }
    }

    @Override
    public boolean deductStock(StockDeduction stockDeduction) {
        log.info("deductStock, start: {}", JSON.toJSONString(stockDeduction));

        // Check if the stock deduction is valid
        if (stockDeduction == null || stockDeduction.invalidParams()) {
            return false;
        }

        try {
            // Build cache keys for accessing the Redis cache
            String stockCacheKey = buildStockCacheKey(stockDeduction.getItemId());
            String stockAlignKey = buildStockAlignKey(stockDeduction.getItemId());
            List<String> keys = Lists.newArrayList(stockCacheKey, stockAlignKey);

            // Prepare the Redis LUA script for stock deduction
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(DEDUCT_STOCK_LUA, Long.class);
            Long result = null;
            long startTime = System.currentTimeMillis();

            // Loop until stock deduction is successful or timeout occurs
            while ((result == null || result == IN_STOCK_ALIGNING) && (System.currentTimeMillis() - startTime) < 1500) {

                // Execute the LUA script and store the result
                result = redisCacheService.getRedisTemplate().execute(redisScript, keys, stockDeduction.getQuantity());
                if (result == null || result == -1 || result == -2 || result == -3) {
                    log.info("deductStock, duduct failed: {}", stockCacheKey);
                    return false;
                }

                // If stock alignment is in progress, wait and retry
                if (result == IN_STOCK_ALIGNING) {
                    log.info("deductStock, stock aligning: {}", stockCacheKey);
                    Thread.sleep(20);
                }

                // Deduction success
                if (result == 1) {
                    log.info("deductStock, deduct success: {}", stockCacheKey);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("deductStock error: ", e);
            return false;
        }

        // Return false if deduction not successful within timeout
        return false;
    }

    @Override
    public boolean revertStock(StockDeduction stockDeduction) {
        log.info("revertStock, start: {}", JSON.toJSONString(stockDeduction));

        // Check if the stock deduction is valid
        if (stockDeduction == null || stockDeduction.invalidParams()) {
            return false;
        }

        try {
            // Build cache keys for accessing the Redis cache
            String stockCacheKey = buildStockCacheKey(stockDeduction.getItemId());
            String stockAlignKey = buildStockAlignKey(stockDeduction.getItemId());
            List<String> keys = Lists.newArrayList(stockCacheKey, stockAlignKey);

            // Prepare the Redis LUA script for stock deduction
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(REVERT_STOCK_LUA, Long.class);
            Long result = null;
            long startTime = System.currentTimeMillis();

            // Loop until stock revert is successful or timeout occurs
            while ((result == null || result == IN_STOCK_ALIGNING) && (System.currentTimeMillis() - startTime) < 1500) {

                // Execute the LUA script and store the result
                result = redisCacheService.getRedisTemplate().execute(redisScript, keys, stockDeduction.getQuantity());
                if (result == null || result == -1) {
                    log.info("revertStock, revert failed: {}", stockCacheKey);
                    return false;
                }

                // If stock alignment is in progress, wait and retry
                if (result == IN_STOCK_ALIGNING) {
                    log.info("revertStock, stock aligning: {}", stockCacheKey);
                    Thread.sleep(20);
                }

                // Revert success
                if (result == 1) {
                    log.info("revertStock, revert success: {}", stockCacheKey);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("revertStock failed");
            return false;
        }

        // Return false if revert not successful within timeout
        return false;
    }

    public static String buildStockAlignKey(Long itemId) {
        return LinkUtil.link(STOCK_ALIGN_LOCK_KEY, itemId);
    }

    public static String buildStockCacheKey(Long itemId) {
        return LinkUtil.link(STOCK_CACHE_KEY, itemId);
    }
}
