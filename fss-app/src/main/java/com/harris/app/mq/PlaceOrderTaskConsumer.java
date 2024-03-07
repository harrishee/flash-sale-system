package com.harris.app.mq;

import com.alibaba.fastjson.JSON;
import com.harris.app.model.PlaceOrderTask;
import com.harris.app.service.placeorder.QueuedPlaceOrderService;
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
public class PlaceOrderTaskConsumer implements RocketMQListener<String> {
    @Resource
    private QueuedPlaceOrderService queuedPlaceOrderService;
    
    @Override
    public void onMessage(String s) {
        // log.info("队列任务消费者 开始，接收下单任务消息: [{}]", s);
        
        if (StringUtils.isEmpty(s)) {
            log.info("队列任务消费者，接收下单任务消息为空: [message={}]", s);
            return;
        }
        
        try {
            // 反序列化消息，并开始消费任务
            PlaceOrderTask placeOrderTask = JSON.parseObject(s, PlaceOrderTask.class);
            queuedPlaceOrderService.handlePlaceOrderTask(placeOrderTask);
            
            // log.info("队列任务消费者，下单任务消费完成: [placeOrderTask={}]", placeOrderTask);
        } catch (Exception e) {
            log.error("队列任务消费者，下单任务处理异常: [message={}] ", s, e);
        }
    }
}
