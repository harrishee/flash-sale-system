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
     * 发送消息
     *
     * @param topic 消息的主题，用于标识消息的分类
     * @param body  消息的内容，以字符串形式传递
     */
    public void sendMessage(String topic, String body) throws Exception {
        Message message = new Message(topic, body.getBytes());
        rocketMQTemplate.getProducer().send(message);
    }

    /**
     * 发送延时消息
     *
     * @param topic          消息的主题，用于标识消息的分类
     * @param body           消息的内容，以字符串形式传递
     * @param delayTimeLevel 延时级别，表示延时时间（由配置决定）
     */
    public void sendDelayMessage(String topic, String body, int delayTimeLevel) throws Exception {
        Message message = new Message(topic, body.getBytes());

        // 开源 RocketMQ 默认支持 18 个 level 的延迟消息：
        // delayTimeLevel = 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
        message.setDelayTimeLevel(delayTimeLevel);
        rocketMQTemplate.getProducer().send(message);
    }
}
