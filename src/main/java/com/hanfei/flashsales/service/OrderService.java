package com.hanfei.flashsales.service;

import com.hanfei.flashsales.pojo.Order;
import com.hanfei.flashsales.vo.Result;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
public interface OrderService {

    Order createOrder(Long userPhone, Long activityId) throws Exception;

    Result payOrder(String orderNo) throws Exception;

    Order getOrderByOrderNo(String orderNo);

    Order createOrderNoMq(String phone, Long activityId);
}
