package com.hanfei.flashsales.service;

import com.hanfei.flashsales.pojo.Order;
import com.hanfei.flashsales.vo.Result;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
public interface OrderService {

    // 没有 MQ 版本
    Order createOrder(Long userId, Long activityId);

    // 有 MQ 版本
    Order createOrderMq(Long userId, Long activityId) throws Exception;

    Result payOrder(String orderNo) throws Exception;

    Order getOrderByOrderNo(String orderNo);
}
