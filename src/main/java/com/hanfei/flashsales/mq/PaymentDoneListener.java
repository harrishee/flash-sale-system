package com.hanfei.flashsales.mq;

import com.alibaba.fastjson.JSON;
import com.hanfei.flashsales.mapper.ActivityMapper;
import com.hanfei.flashsales.pojo.Order;
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
@Transactional
@RocketMQMessageListener(topic = "pay_done", consumerGroup = "pay_done_group")
public class PaymentDoneListener implements RocketMQListener<MessageExt> {

    @Autowired
    private ActivityMapper activityMapper;

    /**
     * Handle successful payment messages
     */
    @Override
    public void onMessage(MessageExt messageExt) {
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        Order order = JSON.parseObject(message, Order.class);

        log.info("Received payment done message: [{}]", order.getOrderNo());
        activityMapper.deductStockById(order.getActivityId());
    }
}
