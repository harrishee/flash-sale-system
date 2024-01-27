package com.harris.app.mq;

import com.alibaba.fastjson.JSON;
import com.harris.app.model.PlaceOrderTask;
import com.harris.app.service.app.impl.QueuedPlaceOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@ConditionalOnProperty(name = "place_order_type", havingValue = "queued")
@RocketMQMessageListener(topic = "PLACE_ORDER_TASK_TOPIC", consumerGroup = "PLACE_ORDER_TASK_TOPIC_CONSUMER_GROUP")
public class RocketMQOrderTaskConsumer implements RocketMQListener<String> {
    @Resource
    private QueuedPlaceOrderService queuedPlaceOrderService;

    @Override
    public void onMessage(String s) {
        log.info("RocketMQOrderTaskConsumer, receive: {}", s);

        if (StringUtils.isEmpty(s)) {
            log.info("RocketMQOrderTaskConsumer, empty message: {}", s);
            return;
        }

        try {
            PlaceOrderTask placeOrderTask = JSON.parseObject(s, PlaceOrderTask.class);
            queuedPlaceOrderService.handlePlaceOrderTask(placeOrderTask);

            log.info("RocketMQOrderTaskConsumer, task done: {}", placeOrderTask.getPlaceOrderTaskId());
        } catch (Exception e) {
            log.error("RocketMQOrderTaskConsumer, task error: ", e);
        }
    }
}
