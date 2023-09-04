package com.hanfei.flashsales.mq;

import com.alibaba.fastjson.JSON;
import com.hanfei.flashsales.controller.SaleController;
import com.hanfei.flashsales.mapper.ActivityMapper;
import com.hanfei.flashsales.mapper.OrderMapper;
import com.hanfei.flashsales.pojo.Order;
import com.hanfei.flashsales.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = "pay_check", consumerGroup = "pay_check_group")
public class PaymentCheckListener implements RocketMQListener<MessageExt> {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private SaleController saleController;

    @Autowired
    private RedisService redisService;

    /**
     * Handle Timeout Orders
     */
    @Override
    @Transactional
    public void onMessage(MessageExt messageExt) {
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        Order orderNoInfo = JSON.parseObject(message, Order.class);
        Order orderInfo = orderMapper.selectOrderByOrderNo(orderNoInfo.getOrderNo());
        log.info("Received order payment status check message: [{}]", orderInfo);

        // If the order is not paid, close the order
        if (orderInfo.getOrderStatus() != 2) {
            log.info("Payment not completed, closing order, orderNo: [{}]", orderInfo.getOrderNo());
            orderInfo.setOrderStatus(-1);
            orderMapper.updateOrder(orderInfo);

            // 1. Revert mysql stock
            activityMapper.revertStockById(orderNoInfo.getActivityId());

            // 2. Revert redis stock
            String key = "activity:" + orderNoInfo.getActivityId();
            redisService.incrementValueByKey(key);

            // 3. Remove user from the Redis list of purchased members
            redisService.removeLimitMember(orderNoInfo.getActivityId(), orderInfo.getUserId());
            log.info("Remove from the purchased list, userId: [{}], activityId: [{}]", orderInfo.getUserId(), orderNoInfo.getActivityId());

            // 4. Revert in-memory flag
            saleController.getEmptyStockMap().put(orderNoInfo.getActivityId(), false);
        }
    }
}
