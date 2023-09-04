package com.hanfei.flashsales.mq;

import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Service
public class MessageSender {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * Send a message
     *
     * @param topic The topic of the message, used to categorize the message
     * @param body  The content of the message in string format
     */
    public void sendMessage(String topic, String body) throws Exception {
        Message message = new Message(topic, body.getBytes());
        rocketMQTemplate.getProducer().send(message);
    }

    /**
     * Send a delayed message
     *
     * @param delayTimeLevel The delay level, representing the delay time (determined by configuration)
     */
    public void sendDelayMessage(String topic, String body, int delayTimeLevel) throws Exception {
        Message message = new Message(topic, body.getBytes());

        // RocketMQ supports 18 levels of delay messages by default:
        // delayTimeLevel = 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
        message.setDelayTimeLevel(delayTimeLevel);
        rocketMQTemplate.getProducer().send(message);
    }
}
