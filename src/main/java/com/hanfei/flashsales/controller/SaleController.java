package com.hanfei.flashsales.controller;

import com.alibaba.fastjson.JSON;
import com.hanfei.flashsales.pojo.Activity;
import com.hanfei.flashsales.pojo.Order;
import com.hanfei.flashsales.pojo.User;
import com.hanfei.flashsales.service.ActivityService;
import com.hanfei.flashsales.service.OrderService;
import com.hanfei.flashsales.service.RedisService;
import com.hanfei.flashsales.vo.Result;
import com.hanfei.flashsales.vo.ResultEnum;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Slf4j
@RestController
@RequestMapping("/sale")
public class SaleController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private RedisService redisService;

    @Getter
    private final Map<Long, Boolean> emptyStockMap = new HashMap<>();

    /**
     * Handle sale request
     * 4. feat: process order by mq
     * 5. feat: in-memory marking
     */
    @PostMapping("/processSaleCacheMq")
    public Result processSaleCacheMq(User user, Long activityId) throws Exception {

        // In-memory marking to reduce Redis access, check if the activity has empty stock
        if (emptyStockMap.get(activityId)) {
            log.info("===> Purchase fail, activity has no stock from Map, userId: [{}]", user.getUserId());
            return Result.error(ResultEnum.EMPTY_STOCK);
        }

        // Check if the user has already purchased to prevent repeated purchases
        if (redisService.isInLimitMember(activityId, user.getUserId())) {
            log.info("===> Purchase fail, already purchased, userId: [{}]", user.getUserId());
            return Result.error(ResultEnum.REPEAT_ERROR);
        }

        // Check and deduct stock using Redis cache to avoid frequent database access.
        boolean deductResult = false;
        deductResult = redisService.stockDeductValidator(activityId);
        if (!deductResult) {
            log.info("===> Purchase fail, activity has no stock from Redis, userId: [{}]", user.getUserId());
            emptyStockMap.put(activityId, true);
            return Result.error(ResultEnum.EMPTY_STOCK);

        } else {
            // If Redis stock is available, use MQ for placing order and asynchronous stock deduction
            Order order = orderService.createOrderMq(user.getUserId(), activityId);

            // Add the user to the purchase limit list
            redisService.addLimitMember(activityId, user.getUserId());
            log.info("Added to the purchase limit list, userId: [{}], activityId: [{}]", user.getUserId(), activityId);

            String orderNo = order.getOrderNo();
            log.info("===> Purchase successful! userId: [{}], orderNo: [{}]", user.getUserId(), orderNo);
            return Result.success(order);
        }
    }

    /**
     * Handle order payment requests, just simulate the payment process
     */
    @RequestMapping("/payOrder/{orderNo}")
    public Result payOrder(@PathVariable String orderNo) throws Exception {
        return orderService.payOrder(orderNo);
    }

    /**
     * 1. feat: basic sell
     */
    @PostMapping("/processSaleNoLock")
    public Result processSaleNoLock(User user, Long activityId) throws Exception {

        // 1. Check available stock
        Activity activity = activityService.getActivityById(activityId);
        Long availableStock = activity.getAvailableStock();
        if (availableStock < 1) {
            // 2.1 If not enough, return
            log.info("===> Purchase fail, activity has no stock, userId: [{}]", user.getUserId());
            return Result.error(ResultEnum.EMPTY_STOCK);
        } else {
            activityService.lockStockNoLock(activityId);

            // 2.2 If enough, place order
            Order order = orderService.createOrder(user.getUserId(), activity.getActivityId());
            String orderNo = order.getOrderNo();
            log.info("===> Purchase successful! userId: [{}], orderNo: [{}]", user.getUserId(), orderNo);
            return Result.success(order);
        }
    }

    /**
     * 2. feat: sql optimistic lock
     */
    @PostMapping("/processSaleOptimisticLock")
    public Result processSaleOptimisticLock(User user, Long activityId) throws Exception {

        // Optimistic locking: Checks if available_stock is greater than 0 while updating the stock
        // If not, it means the product is sold out, and no stock update is performed, avoiding overselling
        boolean lockStockResult = activityService.lockStock(activityId);
        if (!lockStockResult) {
            log.info("===> Purchase fail, activity has no stock, userId: [{}]", user.getUserId());
            return Result.error(ResultEnum.EMPTY_STOCK);
        } else {
            // If stock is available, place order
            Order order = orderService.createOrder(user.getUserId(), activityId);
            String orderNo = order.getOrderNo();
            log.info("===> Purchase successful! userId: [{}], orderNo: [{}]", user.getUserId(), orderNo);
            return Result.success(order);
        }
    }

    /**
     * 3. feat: Redis Lua Script
     */
    @PostMapping("/processSaleCache")
    public Result processSaleCache(User user, Long activityId) throws Exception {

        // Uses Redis caching to check and deduct stock with Lua script. If stock is insufficient, returns an error
        boolean deductResult = false;
        deductResult = redisService.stockDeductValidator(activityId);
        if (!deductResult) {
            log.info("===> Purchase fail, activity has no stock, userId: [{}]", user.getUserId());
            return Result.error(ResultEnum.EMPTY_STOCK);

        } else {
            // If Redis stock is available, lock the stock in the database, and then place order
            activityService.lockStock(activityId);
            Order order = orderService.createOrder(user.getUserId(), activityId);
            String orderNo = order.getOrderNo();

            log.info("===> Purchase successful! userId: [{}], orderNo: [{}]", user.getUserId(), orderNo);
            return Result.success(order);
        }
    }
}
