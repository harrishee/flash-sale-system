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
public class PayCheckListener implements RocketMQListener<MessageExt> {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private SaleController saleController;

    @Autowired
    private RedisService redisService;

    /**
     * 处理超时订单
     *
     * 加 @Transactional 确保数据库操作:
     * 1. 更新数据库中的订单状态：orderMapper.updateOrder(orderInfo)
     * 2. 恢复数据库中的可用库存和锁定库存：activityMapper.incAvailAndDeductLockById(order.getActivityId())
     * 3. 增加 Redis 中的库存：redisService.incrementKey("stock:" + order.getActivityId())
     * 4. 从 Redis 中已购名单中移除用户：redisService.removeLimitMember(order.getActivityId(), order.getUserId())
     * 要么全部成功提交，要么全部回滚
     *
     * @param messageExt 消息对象
     */
    @Override
    @Transactional
    public void onMessage(MessageExt messageExt) {
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        // log.info("***MQ*** 接收到订单支付状态校验消息: " + message + " ***PayCheckListener***");

        Order orderNoInfo = JSON.parseObject(message, Order.class);
        Order orderInfo = orderMapper.selectOrderByOrderNo(orderNoInfo.getOrderNo());

        // 判断订单是否完成支付
        // 订单状态：0:没有库存，无效订单，1:已创建等待支付，2: 已支付购买成功，-1: 未支付已关闭
        if (orderInfo.getOrderStatus() != 2) {
            log.info("***MQ*** 未完成支付，关闭订单，订单号: " + orderInfo.getOrderNo() + " ***PayCheckListener***");
            orderInfo.setOrderStatus(-1);
            orderMapper.updateOrder(orderInfo);

            // 1. 恢复数据库库存
            activityMapper.revertStockById(orderNoInfo.getActivityId());

            // 2. 恢复 redis 库存
            String key = "activity:" + orderNoInfo.getActivityId();
            redisService.incrementValueByKey(key);

            // 3. 将用户从已购名单中移除
            redisService.removeLimitMember(orderNoInfo.getActivityId(), orderInfo.getUserId());

            // 4. 恢复内存标记
            saleController.getEmptyStockMap().put(orderNoInfo.getActivityId(), false);
        }
    }
}
