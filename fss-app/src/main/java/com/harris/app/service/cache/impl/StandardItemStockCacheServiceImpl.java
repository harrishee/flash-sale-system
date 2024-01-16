package com.harris.app.service.cache.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.harris.app.model.cache.ItemStockCache;
import com.harris.app.service.cache.ItemStockCacheService;
import com.harris.app.util.PlaceOrderTypeCondition;
import com.harris.domain.model.StockDeduction;
import com.harris.domain.model.entity.FlashItem;
import com.harris.domain.service.FlashItemDomainService;
import com.harris.infra.cache.DistributedCacheService;
import com.harris.infra.cache.RedisCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.harris.infra.util.StringUtil.link;

@Slf4j
@Service
@Conditional(PlaceOrderTypeCondition.class)
public class StandardItemStockCacheServiceImpl implements ItemStockCacheService {
    private static final int IN_STOCK_ALIGNING = -9;
    private static final String ITEM_STOCKS_CACHE_KEY = "ITEM_STOCKS_CACHE_KEY";
    private static final String ITEM_STOCK_ALIGN_LOCK_KEY = "ITEM_STOCK_ALIGN_LOCK_KEY";
    private final static Cache<Long, ItemStockCache> itemStockLocalCache =
            CacheBuilder.newBuilder()
                    .initialCapacity(10)
                    .concurrencyLevel(5)
                    .expireAfterWrite(10, TimeUnit.SECONDS)
                    .build();
    private static final String INIT_OR_ALIGN_ITEM_STOCK_LUA;
    private static final String INCREASE_ITEM_STOCK_LUA;
    private static final String DECREASE_ITEM_STOCK_LUA;

    static {
        INIT_OR_ALIGN_ITEM_STOCK_LUA = "if (redis.call('exists', KEYS[2]) == 1) then" +
                "    return -997;" +
                "end;" +
                "redis.call('set', KEYS[2] , 1);" +
                "local stockNumber = tonumber(ARGV[1]);" +
                "redis.call('set', KEYS[1] , stockNumber);" +
                "redis.call('del', KEYS[2]);" +
                "return 1";

        INCREASE_ITEM_STOCK_LUA = "if (redis.call('exists', KEYS[2]) == 1) then" +
                "    return -9;" +
                "end;" +
                "if (redis.call('exists', KEYS[1]) == 1) then" +
                "    local stock = tonumber(redis.call('get', KEYS[1]));" +
                "    local num = tonumber(ARGV[1]);" +
                "    redis.call('incrby', KEYS[1] , num);" +
                "    return 1;" +
                "end;" +
                "return -1;";


        DECREASE_ITEM_STOCK_LUA = "if (redis.call('exists', KEYS[2]) == 1) then" +
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
    private FlashItemDomainService flashItemDomainService;

    @Override
    public ItemStockCache getAvailableItemStock(Long userId, Long itemId) {
        ItemStockCache itemStockCache = itemStockLocalCache.getIfPresent(itemId);
        if (itemStockCache != null) {
            return itemStockCache;
        }
        Integer availableStock = distributedCacheService.getObject(buildItemStocksCacheKey(itemId), Integer.class);
        if (availableStock == null) {
            return null;
        }
        itemStockCache = new ItemStockCache().with(availableStock);
        itemStockLocalCache.put(itemId, itemStockCache);
        return itemStockCache;
    }

    @Override
    public boolean alignItemStocks(Long itemId) {
        if (itemId == null) {
            log.info("alignItemStocks, no itemId");
            return false;
        }
        try {
            FlashItem flashItem = flashItemDomainService.getItem(itemId);
            if (flashItem == null) {
                log.info("alignItemStocks, item not exist: {}", itemId);
                return false;
            }
            if (flashItem.getInitialStock() == null) {
                log.info("alignItemStocks, item stock not set up: {}", itemId);
                return false;
            }

            String itemStocksCacheKey = buildItemStocksCacheKey(itemId);
            String itemStocksAlignKey = buildItemStocksCacheAlignKey(itemId);
            List<String> keys = Lists.newArrayList(itemStocksCacheKey, itemStocksAlignKey);
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(INIT_OR_ALIGN_ITEM_STOCK_LUA, Long.class);
            Long result = redisCacheService.getRedisTemplate().execute(redisScript, keys, flashItem.getAvailableStock());
            if (result == null) {
                log.info("alignItemStocks, alignment failed: {},{},{}", itemId, itemStocksCacheKey, flashItem.getInitialStock());
                return false;
            }
            if (result == -997) {
                log.info("alignItemStocks, alignment canceled: {},{},{},{}", result, itemId, itemStocksCacheKey, flashItem.getInitialStock());
                return true;
            }
            if (result == 1) {
                log.info("alignItemStocks, alignment done: {},{},{},{}", result, itemId, itemStocksCacheKey, flashItem.getInitialStock());
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean decreaseItemStock(StockDeduction stockDeduction) {
        if (stockDeduction == null || !stockDeduction.validParams()) {
            return false;
        }
        try {
            String itemStocksCacheKey = buildItemStocksCacheKey(stockDeduction.getItemId());
            String itemStocksAlignKey = buildItemStocksCacheAlignKey(stockDeduction.getItemId());
            List<String> keys = Lists.newArrayList(itemStocksCacheKey, itemStocksAlignKey);
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(DECREASE_ITEM_STOCK_LUA, Long.class);
            Long result = null;
            long startTime = System.currentTimeMillis();
            while ((result == null || result == IN_STOCK_ALIGNING) && (System.currentTimeMillis() - startTime) < 1500) {
                result = redisCacheService.getRedisTemplate().execute(redisScript, keys, stockDeduction.getQuantity());
                if (result == null || result == -1 || result == -2 || result == -3) {
                    log.info("decreaseItemStock failed: {}", itemStocksCacheKey);
                    return false;
                }
                if (result == IN_STOCK_ALIGNING) {
                    log.info("decreaseItemStock aligning: {}", itemStocksCacheKey);
                    Thread.sleep(20);
                }
                if (result == 1) {
                    log.info("decreaseItemStock done: {}", itemStocksCacheKey);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("decreaseItemStock failed");
            return false;
        }
        return false;
    }

    @Override
    public boolean increaseItemStock(StockDeduction stockDeduction) {
        if (stockDeduction == null || !stockDeduction.validParams()) {
            return false;
        }
        try {
            String itemStocksCacheKey = buildItemStocksCacheKey(stockDeduction.getItemId());
            String itemStocksAlignKey = buildItemStocksCacheAlignKey(stockDeduction.getItemId());
            List<String> keys = Lists.newArrayList(itemStocksCacheKey, itemStocksAlignKey);
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(INCREASE_ITEM_STOCK_LUA, Long.class);
            Long result = null;
            long startTime = System.currentTimeMillis();
            while ((result == null || result == IN_STOCK_ALIGNING) && (System.currentTimeMillis() - startTime) < 1500) {
                result = redisCacheService.getRedisTemplate().execute(redisScript, keys, stockDeduction.getQuantity());
                if (result == null || result == -1) {
                    log.info("increaseItemStock failed: {}", itemStocksCacheKey);
                    return false;
                }
                if (result == IN_STOCK_ALIGNING) {
                    log.info("increaseItemStock aligning: {}", itemStocksCacheKey);
                    Thread.sleep(20);
                }
                if (result == 1) {
                    log.info("increaseItemStock done: {}", itemStocksCacheKey);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("increaseItemStock failed");
            return false;
        }
        return false;
    }

    public static String buildItemStocksCacheAlignKey(Long itemId) {
        return link(ITEM_STOCK_ALIGN_LOCK_KEY, itemId);
    }

    public static String buildItemStocksCacheKey(Long itemId) {
        return link(ITEM_STOCKS_CACHE_KEY, itemId);
    }
}
