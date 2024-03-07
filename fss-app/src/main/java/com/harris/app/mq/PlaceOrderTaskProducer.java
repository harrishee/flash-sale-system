package com.harris.app.mq;

import com.alibaba.fastjson.JSON;
import com.harris.app.model.PlaceOrderTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@ConditionalOnProperty(name = "place_order_type", havingValue = "queued")
public class PlaceOrderTaskProducer {
    private DefaultMQProducer placeOrderMQProducer;
    
    @Value("${rocketmq.placeorder.producer.group}")
    private String producerGroup;
    
    @Value("${rocketmq.name-server}")
    private String nameServer;
    
    @Value("${rocketmq.placeorder.topic}")
    private String placeOrderTopic;
    
    @PostConstruct
    public void init() {
        try {
            // 初始化下单任务生产者
            placeOrderMQProducer = new DefaultMQProducer(producerGroup);
            placeOrderMQProducer.setNamesrvAddr(nameServer);
            placeOrderMQProducer.start();
            
            log.info("初始化队列下单任务生产者成功: [nameServer={}, producerGroup={}, placeOrderTopic={}]", nameServer, producerGroup, placeOrderTopic);
        } catch (Exception e) {
            log.error("初始化队列下单任务生产者失败: [nameServer={}, producerGroup={}, placeOrderTopic={}]", nameServer, producerGroup, placeOrderTopic, e);
        }
    }
    
    public boolean post(PlaceOrderTask placeOrderTask) {
        // log.info("队列任务生产者，投递下单任务: [placeOrderTask={}]", placeOrderTask);
        if (placeOrderTask == null) {
            log.info("队列任务生产者，投递下单任务参数错误");
            return false;
        }
        
        // 序列化并构建消息
        String placeOrderTaskString = JSON.toJSONString(placeOrderTask);
        Message message = new Message();
        message.setTopic(placeOrderTopic);
        message.setBody(placeOrderTaskString.getBytes());
        
        try {
            // 发送消息
            SendResult sendResult = placeOrderMQProducer.send(message);
            if (SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                // log.info("队列任务生产者，下单任务投递成功: [placeOrderTask={}]", placeOrderTask);
                return true;
            } else {
                log.info("队列任务生产者，下单任务投递失败: [placeOrderTask={}]", placeOrderTask);
                return false;
            }
        } catch (Exception e) {
            log.error("队列任务生产者，下单任务投递异常: [placeOrderTask={}] ", placeOrderTask, e);
            return false;
        }
    }
}
