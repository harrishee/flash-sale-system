package com.harris.app.service.app.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.harris.app.exception.AppErrorCode;
import com.harris.app.model.PlaceOrderTask;
import com.harris.app.model.PlaceOrderTaskStatus;
import com.harris.app.model.cache.CacheConstant;
import com.harris.app.model.cache.StockCache;
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
    // 锁的 key 的前缀
    private static final String TOKEN_REFRESH_LOCK_KEY = "TOKEN_REFRESH_LOCK_KEY_";
    private static final String PLACE_ORDER_TASK_ID_KEY = "PLACE_ORDER_TASK_ID_KEY_";
    private static final String PLACE_ORDER_TASK_AVAILABLE_TOKEN_KEY = "PLACE_ORDER_TASK_AVAILABLE_TOKEN_KEY_";
    private static final String DECREMENT_TOKEN_LUA;
    private static final String INCREMENT_TOKEN_LUA;
    
    // 本地缓存，用于临时存储订单令牌数，以减少对外部系统的访问次数
    private static final Cache<Long, Integer> ORDER_TOKEN_LOCAL_CACHE =
            CacheBuilder.newBuilder()
                    .initialCapacity(20)
                    .concurrencyLevel(5)
                    .expireAfterWrite(20, TimeUnit.MILLISECONDS)
                    .build();
    
    static {
        // 用于从 Redis 中减少可用的订单令牌数
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
        
        // 用于向 Redis 中增加可用的订单令牌数
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
        // 从缓存中获取下单任务的状态
        Integer taskStatus = redisCacheService.getObject(buildOrderTaskKey(placeOrderTaskId), Integer.class);
        return PlaceOrderTaskStatus.getStatusByCode(taskStatus);
    }
    
    @Override
    public OrderSubmitResult submit(PlaceOrderTask placeOrderTask) {
        log.info("应用层 submit: [{}]", placeOrderTask);
        if (placeOrderTask == null) return OrderSubmitResult.error(AppErrorCode.INVALID_PARAMS);
        
        // 检查任务是否已经被提交过
        String taskKey = buildOrderTaskKey(placeOrderTask.getPlaceOrderTaskId());
        Integer taskIdSubmitted = redisCacheService.getObject(taskKey, Integer.class);
        // if (taskIdSubmitted != null) return OrderSubmitResult.error(AppErrorCode.REDUNDANT_SUBMIT);
        
        // 获取可用的订单令牌数
        Integer availableOrderToken = getAvailableOrderTokens(placeOrderTask.getItemId());
        if (availableOrderToken == null || availableOrderToken == 0) {
            log.info("应用层 submit, 库存不足: [availableOrderToken={}, placeOrderTask={}]", availableOrderToken, placeOrderTask);
            return OrderSubmitResult.error(AppErrorCode.ORDER_TOKENS_NOT_AVAILABLE);
        }
        
        // 使用 Lua 脚本处理订单令牌的减少操作
        if (!handleOrderTokenOperation(placeOrderTask, DECREMENT_TOKEN_LUA)) {
            log.info("应用层 submit, 库存扣减失败: [{}]", placeOrderTask);
            return OrderSubmitResult.error(AppErrorCode.ORDER_TOKENS_NOT_AVAILABLE);
        }
        
        // 将下单任务提交到消息队列
        boolean postSuccess = mqOrderTaskProducer.post(placeOrderTask);
        if (!postSuccess) {
            // 如果提交失败，则需要回滚令牌数量
            handleOrderTokenOperation(placeOrderTask, INCREMENT_TOKEN_LUA);
            log.info("应用层 submit, 下单任务提交失败: [{}]", placeOrderTask);
            return OrderSubmitResult.error(AppErrorCode.ORDER_TASK_SUBMIT_FAILED);
        }
        
        // 将任务状态存入缓存，标记为已提交，有效期为 24 小时
        redisCacheService.put(taskKey, 0, CacheConstant.HOURS_24);
        log.info("应用层 submit, 下单任务提交成功: [{}]", placeOrderTask);
        return OrderSubmitResult.ok();
    }
    
    @Override
    public void updateTaskHandleResult(String placeOrderTaskId, boolean result) {
        if (StringUtils.isEmpty(placeOrderTaskId)) return;
        String taskKey = buildOrderTaskKey(placeOrderTaskId);
        
        // 从缓存中获取任务状态
        Integer taskStatus = redisCacheService.getObject(taskKey, Integer.class);
        if (taskStatus == null || taskStatus != 0) return;
        
        // 将任务状态更新为成功或失败
        redisCacheService.put(taskKey, result ? 1 : -1);
    }
    
    private Integer getAvailableOrderTokens(Long itemId) {
        // 从本地缓存中获取可用的订单令牌数
        Integer availableOrderToken = ORDER_TOKEN_LOCAL_CACHE.getIfPresent(itemId);
        System.out.println("availableOrderToken " + availableOrderToken);
        if (availableOrderToken != null) return availableOrderToken;
        
        // 如果本地缓存没有，则从外部系统刷新
        return refreshLocalAvailableTokens(itemId);
    }
    
    private synchronized Integer refreshLocalAvailableTokens(Long itemId) {
        // 再次检查本地缓存，以防止在等待锁的过程中已经被其他线程更新
        Integer availableOrderToken = ORDER_TOKEN_LOCAL_CACHE.getIfPresent(itemId);
        if (availableOrderToken != null) return availableOrderToken;
        
        // 从外部系统获取最新的可用令牌数，并更新本地缓存
        Integer latestToken = redisCacheService.getObject(buildItemAvailableTokenKey(itemId), Integer.class);
        if (latestToken != null) {
            //
            ORDER_TOKEN_LOCAL_CACHE.put(itemId, latestToken);
            return latestToken;
        }
        
        // 如果外部系统也没有，则尝试刷新外部系统中的令牌数
        return refreshLatestAvailableTokens(itemId);
    }
    
    private Integer refreshLatestAvailableTokens(Long itemId) {
        // 获取 Redisson 分布式锁
        DistributedLock distributedLock = distributedLockService.getLock(buildRefreshTokensLockKey(itemId));
        try {
            // 尝试获取分布式锁
            boolean lockSuccess = distributedLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) return null;
            
            // 从库存缓存服务获取最新的库存情况，并根据库存情况更新令牌数
            StockCache stockCache = stockCacheService.getStockCache(null, itemId);
            System.out.println("stockCache " + stockCache);
            if (stockCache != null && stockCache.isSuccess() && stockCache.getAvailableStock() != null) {
                // 计算 最新的可用令牌数为库存数量的 1.5 倍
                Integer latestToken = (int) Math.ceil(stockCache.getAvailableStock() * 1.5);
                System.out.println("latestToken " + latestToken + " availableStockQuantity " + stockCache.getAvailableStock());
                // 将最新的可用令牌数存入缓存，有效期为 24 小时
                redisCacheService.put(buildItemAvailableTokenKey(itemId), latestToken, CacheConstant.HOURS_24);
                // 同样也将最新的可用令牌数存入本地缓存
                ORDER_TOKEN_LOCAL_CACHE.put(itemId, latestToken);
                return latestToken;
            }
        } catch (Exception e) {
            log.error("应用层 refreshLatestAvailableTokens, 刷新 tokens 失败: [{}]", itemId, e);
        } finally {
            distributedLock.unlock();
        }
        return null;
    }
    
    private boolean handleOrderTokenOperation(PlaceOrderTask placeOrderTask, String script) {
        // 存放将要传递给Lua脚本的键
        List<String> keys = new ArrayList<>();
        keys.add(buildItemAvailableTokenKey(placeOrderTask.getItemId()));
        
        // Redis 脚本执行器
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        
        // 尝试最多执行3次操作，以处理可能的并发更新问题
        for (int i = 0; i < 3; i++) {
            // 执行传入的Lua脚本，并传递键列表，脚本用于修改令牌数
            Long result = redisCacheService.getRedisTemplate().execute(redisScript, keys);
            // 如果执行结果为空，表示执行失败，返回false
            if (result == null) return false;
            
            // 如果执行结果为-100，表示需要刷新令牌数，然后继续下一次循环尝试
            if (result == -100) {
                refreshLatestAvailableTokens(placeOrderTask.getItemId());
                continue;
            }
            
            // 如果执行结果为1，表示操作成功，返回true
            return result == 1L;
        }
        
        // 如果执行3次操作都失败，则返回false
        return false;
    }
    
    // 构建用于令牌刷新操作的分布式锁的 key
    private String buildRefreshTokensLockKey(Long itemId) {
        return TOKEN_REFRESH_LOCK_KEY + itemId;
    }
    
    // 构建用于存储下单任务状态的 Redis key
    private String buildOrderTaskKey(String placeOrderTaskId) {
        return PLACE_ORDER_TASK_ID_KEY + placeOrderTaskId;
    }
    
    // 构建用于存储商品可用令牌数的 Redis key
    private String buildItemAvailableTokenKey(Long itemId) {
        return PLACE_ORDER_TASK_AVAILABLE_TOKEN_KEY + itemId;
    }
}
