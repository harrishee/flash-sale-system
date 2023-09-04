package com.hanfei.flashsales.service.impl;

import com.alibaba.fastjson.JSON;
import com.hanfei.flashsales.mapper.ActivityMapper;
import com.hanfei.flashsales.mapper.OrderMapper;
import com.hanfei.flashsales.mq.MessageSender;
import com.hanfei.flashsales.pojo.Activity;
import com.hanfei.flashsales.pojo.Order;
import com.hanfei.flashsales.service.OrderService;
import com.hanfei.flashsales.utils.SnowFlakeUtils;
import com.hanfei.flashsales.vo.Result;
import com.hanfei.flashsales.vo.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private MessageSender messageSender;

    private final SnowFlakeUtils snowFlakeUtils = new SnowFlakeUtils(1, 1);

    @Override
    public Order createOrderMq(Long userId, Long activityId) throws Exception {

        // 1. Create order
        Activity activity = activityMapper.selectActivityById(activityId);
        Order order = new Order();
        order.setOrderNo(String.valueOf(snowFlakeUtils.nextId()));
        order.setOrderAmount(activity.getSalePrice());
        order.setActivityId(activityId);
        order.setUserId(userId);
        order.setCommodityId(activity.getCommodityId());

        // 2. Send the creating order message to the new_order topic message queue
        messageSender.sendMessage("new_order", JSON.toJSONString(order));

        // 3. Send the order payment status check message to the pay_check topic message queue
        messageSender.sendDelayMessage("pay_check", JSON.toJSONString(order), 4);
        return order;
    }

    @Override
    public Order createOrder(Long userId, Long activityId) {
        Activity activity = activityMapper.selectActivityById(activityId);
        Order order = new Order();
        // Use SnowFlake to generate a unique order number
        order.setOrderNo(String.valueOf(snowFlakeUtils.nextId()));
        // Order status:
        // 0: No stock, invalid order
        // 1: Created, awaiting payment
        // 2: Paid, purchase successful
        // -1: Unpaid, closed
        order.setOrderStatus(1);
        order.setOrderAmount(activity.getSalePrice());
        order.setActivityId(activityId);
        order.setUserId(userId);
        order.setCommodityId(activity.getCommodityId());
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insertOrder(order);
        return order;
    }

    @Override
    public Result payOrder(String orderNo) throws Exception {
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        // Check if the order exists and is in the created status
        if (order == null) {
            log.error("Order does not exist, orderNo: [{}]", orderNo);
            return Result.error(ResultEnum.ORDER_NOT_EXIST);
        } else if (order.getOrderStatus() != 1) {
            log.error("Order status is invalid, orderNo: [{}]", orderNo);
            return Result.error(ResultEnum.ORDER_WRONG_STATUS);
        }

        // Order payment completed
        order.setPayTime(LocalDateTime.now());
        order.setOrderStatus(2);
        orderMapper.updateOrder(order);

        // Send the order payment completed message to the pay_done topic message queue
        messageSender.sendMessage("pay_done", JSON.toJSONString(order));
        return Result.success();
    }

    @Override
    public Order getOrderByOrderNo(String orderNo) {
        return orderMapper.selectOrderByOrderNo(orderNo);
    }
}
