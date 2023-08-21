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
        // 1. 创建订单
        Activity activity = activityMapper.selectActivityById(activityId);
        Order order = new Order();
        order.setOrderNo(String.valueOf(snowFlakeUtils.nextId()));
        order.setOrderAmount(activity.getSalePrice());
        order.setActivityId(activityId);
        order.setUserId(userId);
        order.setCommodityId(activity.getCommodityId());

        // 2. 发送 创建订单 消息到 new_order 主题的消息队列
        messageSender.sendMessage("new_order", JSON.toJSONString(order));

        // 3. 发送 订单付款状态校验 消息到 pay_check 主题的消息队列
        messageSender.sendDelayMessage("pay_check", JSON.toJSONString(order), 4);
        return order;
    }

    @Override
    public Order createOrder(Long userId, Long activityId) {
        Activity activity = activityMapper.selectActivityById(activityId);

        Order order = new Order();
        // 使用雪花算法生成订单号，确保订单号全局唯一
        order.setOrderNo(String.valueOf(snowFlakeUtils.nextId()));
        // 订单状态：0:没有库存，无效订单，1:已创建等待支付，2: 已支付购买成功，-1: 未支付已关闭
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
        log.info("***Service*** 接收订单支付完成请求，orderNo: {}", orderNo + " ***payOrderProcess***");
        return Result.success();
    }

    @Override
    public Order getOrderByOrderNo(String orderNo) {
        return orderMapper.selectOrderByOrderNo(orderNo);
    }
}
