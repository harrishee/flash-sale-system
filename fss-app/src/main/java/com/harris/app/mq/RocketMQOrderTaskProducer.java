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
public class RocketMQOrderTaskProducer implements OrderTaskPostService {
    private DefaultMQProducer placeOrderMQProducer;

    @Value("${rocketmq.name-server}")
    private String nameServer;

    @Value("${rocketmq.placeorder.producer.group}")
    private String producerGroup;

    @Value("${rocketmq.placeorder.topic}")
    private String placeOrderTopic;

    @PostConstruct
    public void init() {
        try {
            placeOrderMQProducer = new DefaultMQProducer(producerGroup);
            placeOrderMQProducer.setNamesrvAddr(nameServer);
            placeOrderMQProducer.start();
            log.info("init RocketMQOrderTaskProducer done: {},{},{}", nameServer, producerGroup, placeOrderTopic);
        } catch (Exception e) {
            log.error("init RocketMQOrderTaskProducer error: {},{},{}", nameServer, producerGroup, placeOrderTopic, e);
        }
    }

    @Override
    public boolean post(PlaceOrderTask placeOrderTask) {
        log.info("RocketMQOrderTaskProducer, receive: {}", JSON.toJSONString(placeOrderTask));
        if (placeOrderTask == null) {
            log.info("RocketMQOrderTaskProducer. invalid params");
            return false;
        }
        String placeOrderTaskString = JSON.toJSONString(placeOrderTask);
        Message message = new Message();
        message.setTopic(placeOrderTopic);
        message.setBody(placeOrderTaskString.getBytes());
        try {
            SendResult sendResult = placeOrderMQProducer.send(message);
            log.info("RocketMQOrderTaskProducer, post done: {},{}", placeOrderTask.getPlaceOrderTaskId(), JSON.toJSONString(sendResult));
            if (SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                log.info("RocketMQOrderTaskProducer, post success: {}", placeOrderTask.getPlaceOrderTaskId());
                return true;
            } else {
                log.info("RocketMQOrderTaskProducer, post failed: {}", placeOrderTask.getPlaceOrderTaskId());
                return false;
            }
        } catch (Exception e) {
            log.error("RocketMQOrderTaskProducer, post error: {}", placeOrderTask.getPlaceOrderTaskId(), e);
            return false;
        }
    }
}
