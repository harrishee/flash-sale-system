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
@ConditionalOnProperty(name = "place_order_type", havingValue = "queued") // 仅当 place_order_type 配置为 queued 时，该组件才会被实例化
@RocketMQMessageListener(topic = "PLACE_ORDER_TASK_TOPIC", consumerGroup = "PLACE_ORDER_TASK_TOPIC_CONSUMER_GROUP")
public class RocketMQOrderTaskConsumer implements RocketMQListener<String> {
    @Resource
    private QueuedPlaceOrderService queuedPlaceOrderService; // 注入处理队列式下单服务
    
    @Override
    public void onMessage(String s) {
        log.info("应用层 orderTaskConsumer，接收下单任务消息: [{}]", s);
        
        if (StringUtils.isEmpty(s)) {
            log.info("用用层 orderTaskConsumer，接收下单任务消息为空: [{}]", s);
            return;
        }
        
        try {
            // 将接收到的JSON字符串消息反序列化为 PlaceOrderTask 对象
            PlaceOrderTask placeOrderTask = JSON.parseObject(s, PlaceOrderTask.class);
            
            // 调用队列式下单服务处理下单任务
            queuedPlaceOrderService.handlePlaceOrderTask(placeOrderTask);
            
            log.info("应用层 orderTaskConsumer，下单任务消息处理完成: [{}]", placeOrderTask.getPlaceOrderTaskId());
        } catch (Exception e) {
            log.error("应用层 orderTaskConsumer，下单任务消息处理失败: [{}]", s);
        }
    }
}
