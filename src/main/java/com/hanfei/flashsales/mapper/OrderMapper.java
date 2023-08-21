package com.hanfei.flashsales.mapper;

import com.hanfei.flashsales.pojo.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Mapper
public interface OrderMapper {

    int insertOrder(Order order);

    Order selectOrderByOrderNo(String orderNo);

    Order getOrderByUserIdAndActivityId(String userId, Long activityId);

    int updateOrder(Order order);
}
