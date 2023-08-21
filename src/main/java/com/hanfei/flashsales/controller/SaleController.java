package com.hanfei.flashsales.controller;

import com.hanfei.flashsales.pojo.Activity;
import com.hanfei.flashsales.pojo.Order;
import com.hanfei.flashsales.pojo.User;
import com.hanfei.flashsales.service.ActivityService;
import com.hanfei.flashsales.service.OrderService;
import com.hanfei.flashsales.vo.Result;
import com.hanfei.flashsales.vo.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * 处理抢购请求
     */
    @PostMapping("/processSaleNoLock")
    public Result processSaleNoLock(User user, Long activityId) throws Exception {

        // 1. 先查询商品库存
        Activity activity = activityService.getActivityById(activityId);
        Long availableStock = activity.getAvailableStock();
        Long lockStock = activity.getLockStock();
        if (availableStock < 1) {
            // 2. 如果库存不足，直接返回
            log.info("=====> 抢购失败，已售罄");
            return Result.error(ResultEnum.EMPTY_STOCK);
        } else {
            activityService.lockStockNoLock(activityId);

            // 3. 如果库存充足，执行下单操作
            Order order = orderService.createOrder(user.getUserId(), activity.getActivityId());
            String orderNo = order.getOrderNo();
            log.info("=====> 抢购成功，用户：{}，订单号：{}", user.getUserId(), orderNo);
            return Result.success(order);
        }
    }

    /**
     * 处理订单支付请求
     */
    @RequestMapping("/payOrder/{orderNo}")
    public Result payOrder(@PathVariable String orderNo) throws Exception {
        log.info("***Controller*** 订单支付，订单号：{}", orderNo);
        return orderService.payOrder(orderNo);
    }
}
