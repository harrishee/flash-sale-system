package com.harris.app.service.placeorder;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.harris.app.exception.AppErrorCode;
import com.harris.app.model.PlaceOrderTask;
import com.harris.app.model.PlaceOrderTaskStatus;
import com.harris.app.model.cache.CacheConstant;
import com.harris.app.model.cache.StockCache;
import com.harris.app.model.result.OrderSubmitResult;
import com.harris.app.mq.PlaceOrderTaskProducer;
import com.harris.app.service.stock.StockCacheService;
import com.harris.infra.distributed.cache.RedisCacheService;
import com.harris.infra.distributed.lock.DistributedLock;
import com.harris.infra.distributed.lock.DistributedLockService;
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
    
    // 本地缓存，用于临时存储订单许可 order token
    private static final Cache<Long, Integer> ORDER_TOKEN_LOCAL_CACHE =
            CacheBuilder.newBuilder()
                    .initialCapacity(20)
                    .concurrencyLevel(5)
                    .expireAfterWrite(20, TimeUnit.MILLISECONDS)
                    .build();
    
    static {
        // 用于从 Redis 中减少可用的订单许可
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
        
        // 用于向 Redis 中增加可用的订单许可
        INCREMENT_TOKEN_LUA = "if (redis.call('exists', KEYS[1]) == 1) then" +
                "   redis.call('incrby', KEYS[1], 1);" +
                "   return 1;" +
                "end;" +
                "return -100;";
    }
    
    @Resource
    private StockCacheService stockCacheService;
    
    @Resource
    private PlaceOrderTaskProducer mqOrderTaskProducer;
    
    @Resource
    private RedisCacheService redisCacheService;
    
    @Resource
    private DistributedLockService distributedLockService;
    
    @Override
    public OrderSubmitResult submit(PlaceOrderTask placeOrderTask) {
        if (placeOrderTask == null) return OrderSubmitResult.error(AppErrorCode.INVALID_PARAMS);
        // log.info("应用层 submit 开始: [placeOrderTask={}]", placeOrderTask);
        
        // 检查一个用户是不是多买了同样的商品，key = PLACE_ORDER_TASK_ORDER_ID_KEY + userId
        String taskKey = buildOrderTaskKey(placeOrderTask.getPlaceOrderTaskId());
        Integer taskIdSubmitted = redisCacheService.get(taskKey, Integer.class);
        
        // 先取消，方便测试
        if (taskIdSubmitted != null) return OrderSubmitResult.error(AppErrorCode.REDUNDANT_SUBMIT);
        
        // 检查是否还有可用库存（订单许可 = 库存数量 * 1.5），为了快速响应和减少下个 if 对 Redis 的操作
        Integer availableOrderToken = getAvailableOrderToken(placeOrderTask.getItemId());
        if (availableOrderToken == null || availableOrderToken == 0) {
            // log.info("应用层 submit, 暂无可用库存: [availableOrderToken={}, itemId={}]", availableOrderToken, placeOrderTask.getItemId());
            return OrderSubmitResult.error(AppErrorCode.ORDER_TOKENS_NOT_AVAILABLE);
        }
        
        // 使用 Lua 脚本减少订单许可
        if (!handleOrderTokenLua(placeOrderTask, DECREMENT_TOKEN_LUA)) {
            log.info("应用层 submit, 暂无可用库存: [placeOrderTask={}]", placeOrderTask);
            return OrderSubmitResult.error(AppErrorCode.ORDER_TOKENS_NOT_AVAILABLE);
        }
        
        // 将下单任务提交到消息队列
        boolean postSuccess = mqOrderTaskProducer.post(placeOrderTask);
        if (!postSuccess) {
            // 如果提交失败，则需要回滚订单许可
            handleOrderTokenLua(placeOrderTask, INCREMENT_TOKEN_LUA);
            log.info("应用层 submit, 下单任务提交失败: [placeOrderTask={}]", placeOrderTask);
            return OrderSubmitResult.error(AppErrorCode.ORDER_TASK_SUBMIT_FAILED);
        }
        
        // 将任务状态存入缓存，标记为已提交，有效期为 24 小时
        redisCacheService.put(taskKey, 0, CacheConstant.HOURS_24);
        // log.info("应用层 submit 结束, 下单任务提交成功: [placeOrderTask={}]", placeOrderTask);
        return OrderSubmitResult.ok();
    }
    
    private Integer getAvailableOrderToken(Long itemId) {
        // 从本地缓存中获取可用的 订单许可数量，有的话直接返回
        Integer availableOrderToken = ORDER_TOKEN_LOCAL_CACHE.getIfPresent(itemId);
        if (availableOrderToken != null) {
            // log.info("应用层 getAvailableOrderToken, 本地缓存命中: [itemId={}, availableOrderToken={}]", itemId, availableOrderToken);
            return availableOrderToken;
        }
        
        // 如果本地缓存没有，从分布式缓存中获取
        // log.info("应用层 getAvailableOrderToken, 本地缓存未命中: [itemId={}]", itemId);
        return refreshLocalAvailableToken(itemId);
    }
    
    private synchronized Integer refreshLocalAvailableToken(Long itemId) {
        // 再次检查本地缓存，以防止在等待锁的过程中已经被其他线程更新
        Integer availableOrderToken = ORDER_TOKEN_LOCAL_CACHE.getIfPresent(itemId);
        if (availableOrderToken != null) return availableOrderToken;
        
        // 本地缓存中依旧没有，从分布式缓存中获取，key = PLACE_ORDER_TASK_AVAILABLE_TOKEN_KEY + itemId
        Integer latestToken = redisCacheService.get(buildItemAvailableTokenKey(itemId), Integer.class);
        if (latestToken != null) {
            // 将分布式缓存中的最新 订单许可数量 存入本地缓存
            ORDER_TOKEN_LOCAL_CACHE.put(itemId, latestToken);
            return latestToken;
        }
        
        // 如果分布式缓存中也没有，说明是第一次获取，需要刷新最新的 订单许可数量
        return refreshLatestAvailableToken(itemId);
    }
    
    private Integer refreshLatestAvailableToken(Long itemId) {
        // 获取分布式锁实例，防止 itemId 的订单许可数量并发更新，key = TOKEN_REFRESH_LOCK_KEY + itemId
        DistributedLock rLock = distributedLockService.getLock(buildRefreshTokensLockKey(itemId));
        try {
            // 尝试获取分布式锁
            boolean lockSuccess = rLock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!lockSuccess) return null;
            
            // 从库存缓存服务获取 库存
            StockCache stockCache = stockCacheService.getStockCache(null, itemId);
            if (stockCache != null && stockCache.isSuccess() && stockCache.getAvailableStock() != null) {
                // 订单许可数量 = 库存数量 * 1.5
                Integer latestToken = (int) Math.ceil(stockCache.getAvailableStock() * 1.5);
                
                // 更新 订单许可数量 到分布式和本地缓存中
                redisCacheService.put(buildItemAvailableTokenKey(itemId), latestToken, CacheConstant.HOURS_24);
                ORDER_TOKEN_LOCAL_CACHE.put(itemId, latestToken);
                
                // log.info("应用层 refreshLatestAvailableToken, 刷新订单许可成功: [itemId={}, latestToken={}]", itemId, latestToken);
                return latestToken;
            }
        } catch (Exception e) {
            log.error("应用层 refreshLatestAvailableToken, 刷新订单许可异常: [itemId={}] ", itemId, e);
        } finally {
            rLock.unlock();
        }
        return null;
    }
    
    @Override
    public void updatePlaceOrderTaskHandleResult(String placeOrderTaskId, boolean result) {
        if (StringUtils.isEmpty(placeOrderTaskId)) return;
        
        // 任务状态 key = PLACE_ORDER_TASK_ID_KEY + placeOrderTaskId
        String taskKey = buildOrderTaskKey(placeOrderTaskId);
        Integer taskStatus = redisCacheService.get(taskKey, Integer.class);
        if (taskStatus == null || taskStatus != 0) return;
        
        // 更新任务状态到分布式缓存中，1 表示成功，-1 表示失败
        redisCacheService.put(taskKey, result ? 1 : -1);
    }
    
    private boolean handleOrderTokenLua(PlaceOrderTask placeOrderTask, String script) {
        List<String> keys = new ArrayList<>();
        keys.add(buildItemAvailableTokenKey(placeOrderTask.getItemId()));
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        
        // 尝试最多执行 3 次操作:
        // 系统难免有意外发生（比如扣减时宕机等），为了保证可用下单许可数量的有效性，给下单许可设置了过期时间，这会导致在执行 LUA 脚本时数据不存在。
        // 所以，在数据不存在时当前线程会主动尝试刷新数据，然后继续执行LUA脚本。
        // 也就是当用户抢到了下单许可但是下单失败或取消订单时，系统会定时对数据进行纠正，腾出来空余的许可给后面需要的用户，确保所有库存均可对外销售。
        for (int i = 0; i < 3; i++) {
            Long result = redisCacheService.getRedisTemplate().execute(redisScript, keys);
            // 如果执行结果为空，表示执行失败
            if (result == null) return false;
            
            // 如果执行结果为 -100，表示数据不存在，需要刷新最新可用许可数量
            if (result == -100) {
                refreshLatestAvailableToken(placeOrderTask.getItemId());
                continue;
            }
            
            return result == 1L;
        }
        
        // 如果执行3次操作都失败，则返回false
        return false;
    }
    
    @Override
    public PlaceOrderTaskStatus getStatus(String placeOrderTaskId) {
        // 从缓存中获取下单任务的状态
        Integer taskStatus = redisCacheService.get(buildOrderTaskKey(placeOrderTaskId), Integer.class);
        return PlaceOrderTaskStatus.getStatusByCode(taskStatus);
    }
    
    private String buildRefreshTokensLockKey(Long itemId) {
        return TOKEN_REFRESH_LOCK_KEY + itemId;
    }
    
    private String buildOrderTaskKey(String placeOrderTaskId) {
        return PLACE_ORDER_TASK_ID_KEY + placeOrderTaskId;
    }
    
    private String buildItemAvailableTokenKey(Long itemId) {
        return PLACE_ORDER_TASK_AVAILABLE_TOKEN_KEY + itemId;
    }
}
