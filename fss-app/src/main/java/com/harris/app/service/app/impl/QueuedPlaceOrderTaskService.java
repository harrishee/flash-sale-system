package com.harris.app.service.app.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.harris.app.exception.AppErrorCode;
import com.harris.app.model.PlaceOrderTask;
import com.harris.app.model.cache.CacheConstant;
import com.harris.app.model.cache.StockCache;
import com.harris.app.model.enums.PlaceOrderTaskStatus;
import com.harris.app.model.result.OrderSubmitResult;
import com.harris.app.mq.RocketMQOrderTaskProducer;
import com.harris.app.service.app.PlaceOrderTaskService;
import com.harris.app.service.cache.StockCacheService;
import com.harris.infra.cache.RedisCacheService;
import com.harris.infra.lock.DistributedLock;
import com.harris.infra.lock.DistributedLockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@ConditionalOnProperty(name = "place_order_type", havingValue = "queued")
public class QueuedPlaceOrderTaskService implements PlaceOrderTaskService {
    private static final String TOKEN_REFRESH_LOCK_KEY = "TOKEN_REFRESH_LOCK_KEY_";
    private static final String PLACE_ORDER_TASK_ID_KEY = "PLACE_ORDER_TASK_ID_KEY_";
    private static final String PLACE_ORDER_TASK_AVAILABLE_TOKEN_KEY = "PLACE_ORDER_TASK_AVAILABLE_TOKEN_KEY_";
    private static final String DECREMENT_TOKEN_LUA;
    private static final String INCREMENT_TOKEN_LUA;
    private final static Cache<Long, Integer> tokenLocalCache =
            CacheBuilder.newBuilder()
                    .initialCapacity(20)
                    .concurrencyLevel(5)
                    .expireAfterWrite(20, TimeUnit.MILLISECONDS)
                    .build();

    static {
        DECREMENT_TOKEN_LUA = "if (redis.call('exists', KEYS[1]) == 1) then" +
                "    local availableTokensCount = tonumber(redis.call('get', KEYS[1]));" +
                "    if (availableTokensCount == 0) then" +
                "        return -1;" +
                "    end;" +
                "    if (availableTokensCount > 0) then" +
                "        redis.call('incrby', KEYS[1], -1);" +
                "        return 1;" +
                "    end;" +
                "end;" +
                "return -100;";

        INCREMENT_TOKEN_LUA = "if (redis.call('exists', KEYS[1]) == 1) then" +
                "   redis.call('incrby', KEYS[1], 1);" +
                "   return 1;" +
                "end;" +
                "return -100;";
    }

    @Resource
    private RedisCacheService redisCacheService;

    @Resource
    private StockCacheService stockCacheService;

    @Resource
    private RocketMQOrderTaskProducer mqOrderTaskProducer;

    @Resource
    private DistributedLockService distributedLockService;

    @Override
    public PlaceOrderTaskStatus getStatus(String placeOrderTaskId) {
        Integer taskStatus = redisCacheService.getObject(buildOrderTaskKey(placeOrderTaskId), Integer.class);
        return PlaceOrderTaskStatus.getStatusByCode(taskStatus);
    }

    @Override
    public OrderSubmitResult submit(PlaceOrderTask placeOrderTask) {
        log.info("App submit task, start: {}", JSON.toJSONString(placeOrderTask));

        // Validate params
        if (placeOrderTask == null) {
            return OrderSubmitResult.error(AppErrorCode.INVALID_PARAMS);
        }

        // Generate a unique task key for the order task and check if it's already submitted
        String taskKey = buildOrderTaskKey(placeOrderTask.getPlaceOrderTaskId());
        Integer taskIdsubmitted = redisCacheService.getObject(taskKey, Integer.class);
        if (taskIdsubmitted != null) {
            return OrderSubmitResult.error(AppErrorCode.REDUNDANT_SUBMIT);
        }

        // Check the availability of order tokens for the item
        // This checks the local cache first and then the Redis cache to
        // find out the current number of available order tokens for the given item
        Integer availableOrderToken = getAvailableOrderTokens(placeOrderTask.getItemId());
        if (availableOrderToken == null || availableOrderToken == 0) {
            return OrderSubmitResult.error(AppErrorCode.ORDER_TOKENS_NOT_AVAILABLE);
        }

        // Attempt to take an order token for the place order task
        if (!handleOrderTokenOperation(placeOrderTask, DECREMENT_TOKEN_LUA)) {
            log.info("App submit task, stock deduct failed: {},{}",
                    placeOrderTask.getUserId(), placeOrderTask.getPlaceOrderTaskId());
            return OrderSubmitResult.error(AppErrorCode.ORDER_TOKENS_NOT_AVAILABLE);
        }

        // Post the order task to the MQ
        boolean postSuccess = mqOrderTaskProducer.post(placeOrderTask);
        if (!postSuccess) {
            // If the post fails, recover the order token
            handleOrderTokenOperation(placeOrderTask, INCREMENT_TOKEN_LUA);
            log.info("App submit task, post task failed: {},{}",
                    placeOrderTask.getUserId(), placeOrderTask.getPlaceOrderTaskId());
            return OrderSubmitResult.error(AppErrorCode.ORDER_TASK_SUBMIT_FAILED);
        }

        // Post the order task successfully, set the task key to 0 and valid for 24 hours
        redisCacheService.put(taskKey, 0, CacheConstant.HOURS_24);
        log.info("App submit task, success: {},{}",
                placeOrderTask.getUserId(), placeOrderTask.getPlaceOrderTaskId());
        return OrderSubmitResult.ok();
    }

    @Override
    public void updateHandleResult(String placeOrderTaskId, boolean result) {
        if (StringUtils.isEmpty(placeOrderTaskId)) {
            return;
        }

        // Retrieve the current task status from Redis
        String taskKey = buildOrderTaskKey(placeOrderTaskId);
        Integer taskStatus = redisCacheService.getObject(taskKey, Integer.class);

        // Only update if the task status is initial state: 0
        if (taskStatus == null || taskStatus != 0) {
            return;
        }

        // Update the task status in Redis: 1 for successful handling, -1 for failure
        redisCacheService.put(taskKey, result ? 1 : -1);
    }

    /**
     *
     */
    private Integer getAvailableOrderTokens(Long itemId) {
        Integer availableOrderToken = tokenLocalCache.getIfPresent(itemId);
        if (availableOrderToken != null) {
            return availableOrderToken;
        }

        // If the local cache doesn't have the latest available token quantity, refresh the local cache
        return refreshLocalAvailableTokens(itemId);
    }

    private synchronized Integer refreshLocalAvailableTokens(Long itemId) {
        Integer availableOrderToken = tokenLocalCache.getIfPresent(itemId);
        if (availableOrderToken != null) {
            return availableOrderToken;
        }

        // Get the latest available token from the Redis cache
        Integer latestToken = redisCacheService.getObject(buildItemAvailableTokenKey(itemId), Integer.class);
        if (latestToken != null) {
            // Put the latest available token into the local cache
            tokenLocalCache.put(itemId, latestToken);
            return latestToken;
        }

        // If no latest available token is found in the Redis cache, refresh the latest available token
        return refreshLatestAvailableTokens(itemId);
    }

    private Integer refreshLatestAvailableTokens(Long itemId) {
        DistributedLock distributedLock = distributedLockService.getDistributedLock(buildRefreshTokensLockKey(itemId));
        try {
            // Try to acquire the lock, wait for 500 milliseconds, with a timeout of 1000 milliseconds
            boolean lockSuccess = distributedLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) {
                return null;
            }

            // Get the item stock cache
            StockCache stockCache = stockCacheService.getStockCache(null, itemId);
            if (stockCache != null && stockCache.isSuccess() && stockCache.getAvailableStockQuantity() != null) {
                // Calculate the latest available token quantity
                // Set the latest available token quantity to 1.5 times the available stock quantity
                Integer latestToken = (int) Math.ceil(stockCache.getAvailableStockQuantity() * 1.5);
                // Put the latest available token quantity into the Redis cache with a validity period of 24 hours
                redisCacheService.put(buildItemAvailableTokenKey(itemId), latestToken, CacheConstant.HOURS_24);
                // Put the latest available token quantity into the local cache
                tokenLocalCache.put(itemId, latestToken);
                return latestToken;
            }
        } catch (Exception e) {
            log.error("App refreshAvailableTokens, refresh tokens failed: {}", itemId, e);
        } finally {
            distributedLock.unlock();
        }
        return null;
    }

    private boolean handleOrderTokenOperation(PlaceOrderTask placeOrderTask, String script) {
        // Create a list to hold Redis keys
        List<String> keys = new ArrayList<>();
        keys.add(buildItemAvailableTokenKey(placeOrderTask.getItemId()));

        // Prepare to execute the Lua script
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);

        // Try executing the Lua script up to three times
        for (int i = 0; i < 3; i++) {
            // Execute the Lua script and get the result
            Long result = redisCacheService.getRedisTemplate().execute(redisScript, keys);
            // If the execution result is null, it indicates a failure
            if (result == null) {
                return false;
            }

            // If the result is -100, it indicates a need to refresh the token count and retry
            if (result == -100) {
                refreshLatestAvailableTokens(placeOrderTask.getItemId());
                continue;
            }

            // If the result is 1, it indicates a success
            return result == 1L;
        }

        // If all attempts fail, return false
        return false;
    }

    private String buildOrderTaskKey(String placeOrderTaskId) {
        return PLACE_ORDER_TASK_ID_KEY + placeOrderTaskId;
    }

    private String buildRefreshTokensLockKey(Long itemId) {
        return TOKEN_REFRESH_LOCK_KEY + itemId;
    }

    private String buildItemAvailableTokenKey(Long itemId) {
        return PLACE_ORDER_TASK_AVAILABLE_TOKEN_KEY + itemId;
    }
}
