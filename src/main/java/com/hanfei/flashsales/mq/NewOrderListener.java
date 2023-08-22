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
     * 处理新订单的创建
     * 加 @Transactional 确保数据库事务:
     * 1. 插入订单：orderMapper.insertOrder(order)
     * 2. 锁定活动库存：lockStock(order.getActivityId())
     * 要么全部成功提交，要么全部回滚
     *
     * @param messageExt 消息对象，包含了消息的内容、主题、标签等信息
     */
    @Override
    @Transactional
    public void onMessage(MessageExt messageExt) {
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        Order order = JSON.parseObject(message, Order.class);
        order.setCreateTime(LocalDateTime.now());

        // 订单状态：0:没有库存，无效订单，1:已创建等待支付，2: 已支付购买成功，-1: 未支付已关闭
        boolean lockStockResult = activityMapper.lockStockById(order.getActivityId());
        if (lockStockResult) {
            order.setOrderStatus(1);
        } else {
            order.setOrderStatus(0);
        }
        log.info("***MQ*** 创建新订单: {}，状态：{}", order.getOrderNo(), order.getOrderStatus());
        orderMapper.insertOrder(order);
    }
}
