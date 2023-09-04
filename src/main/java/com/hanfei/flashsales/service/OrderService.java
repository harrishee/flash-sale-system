package com.hanfei.flashsales.service;

import com.hanfei.flashsales.pojo.Order;
import com.hanfei.flashsales.vo.Result;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
public interface OrderService {

    // No MQ version
    Order createOrder(Long userId, Long activityId);

    // MQ version
    Order createOrderMq(Long userId, Long activityId) throws Exception;

    Result payOrder(String orderNo) throws Exception;

    Order getOrderByOrderNo(String orderNo);
}
