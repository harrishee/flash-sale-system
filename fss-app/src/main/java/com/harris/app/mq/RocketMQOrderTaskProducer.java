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
@ConditionalOnProperty(name = "place_order_type", havingValue = "queued") // 仅当 place_order_type 配置为 queued 时，该组件才会被实例化
public class RocketMQOrderTaskProducer {
    private DefaultMQProducer placeOrderMQProducer; // // 定义 RocketMQ 生产者
    
    @Value("${rocketmq.placeorder.producer.group}")
    private String producerGroup;
    
    @Value("${rocketmq.name-server}")
    private String nameServer;
    
    @Value("${rocketmq.placeorder.topic}")
    private String placeOrderTopic;
    
    @PostConstruct // 在Spring容器初始化该组件后，执行初始化方法
    public void init() {
        try {
            // 初始化下单任务生产者
            placeOrderMQProducer = new DefaultMQProducer(producerGroup);
            placeOrderMQProducer.setNamesrvAddr(nameServer);
            placeOrderMQProducer.start();
            
            log.info("应用层 orderTaskProducer，初始化下单任务生产者成功: [{},{},{}]", nameServer, producerGroup, placeOrderTopic);
        } catch (Exception e) {
            log.error("应用层 orderTaskProducer，初始化下单任务生产者失败: [{},{},{}]", nameServer, producerGroup, placeOrderTopic, e);
        }
    }
    
    public boolean post(PlaceOrderTask placeOrderTask) {
        // log.info("应用层 post，投递下单任务: [{}]", placeOrderTask);
        if (placeOrderTask == null) {
            log.info("应用层 post，投递下单任务参数错误");
            return false;
        }
        
        // 将下单任务对象序列化为JSON字符串
        String placeOrderTaskString = JSON.toJSONString(placeOrderTask);
        
        // 根据下单任务主题和下单任务字符串创建消息对象
        Message message = new Message();
        message.setTopic(placeOrderTopic);
        message.setBody(placeOrderTaskString.getBytes());
        
        try {
            // 发送消息，并获取发送结果
            SendResult sendResult = placeOrderMQProducer.send(message);
            
            // 根据发送结果判断是否发送成功
            if (SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                // log.info("应用层 post，下单任务投递成功: [{}]", placeOrderTask.getPlaceOrderTaskId());
                return true;
            } else {
                log.info("应用层 post，下单任务投递失败: [{}]", placeOrderTask.getPlaceOrderTaskId());
                return false;
            }
        } catch (Exception e) {
            log.error("应用层 post，下单任务投递异常: [{}]", placeOrderTask.getPlaceOrderTaskId(), e);
            return false;
        }
    }
}
