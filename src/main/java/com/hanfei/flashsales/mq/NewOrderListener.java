package com.hanfei.flashsales.mq;

import com.alibaba.fastjson.JSON;
import com.hanfei.flashsales.mapper.ActivityMapper;
import com.hanfei.flashsales.mapper.OrderMapper;
import com.hanfei.flashsales.pojo.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = "new_order", consumerGroup = "order_group")
public class NewOrderListener implements RocketMQListener<MessageExt> {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ActivityMapper activityMapper;

    /**
     * Process the creation of a new order
     */
    @Override
    @Transactional
    public void onMessage(MessageExt messageExt) {
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        Order order = JSON.parseObject(message, Order.class);
        order.setCreateTime(LocalDateTime.now());

        // Order status:
        // 0: No stock, invalid order
        // 1: Created, awaiting payment
        // 2: Paid, purchase successful
        // -1: Unpaid, closed
        boolean lockStockResult = activityMapper.lockStockById(order.getActivityId());
        if (lockStockResult) {
            order.setOrderStatus(1);
        } else {
            order.setOrderStatus(0);
        }
        log.info("Creating new order, orderNo: [{}], orderStatus: [{}]", order.getOrderNo(), order.getOrderStatus());
        orderMapper.insertOrder(order);
    }
}
