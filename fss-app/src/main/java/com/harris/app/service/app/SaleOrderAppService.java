package com.harris.app.service.app;

import com.harris.app.model.command.PlaceOrderCommand;
import com.harris.app.model.dto.SaleOrderDTO;
import com.harris.app.model.query.SaleOrdersQuery;
import com.harris.app.model.result.*;

public interface SaleOrderAppService {
    // 下单操作
    AppSingleResult<PlaceOrderResult> placeOrder(Long userId, PlaceOrderCommand placeOrderCommand);
    
    // 获取下单任务结果
    AppSingleResult<OrderHandleResult> getPlaceOrderTaskResult(Long userId, Long itemId, String placeOrderTaskId);
    
    // 根据用户查询订单列表
    AppMultiResult<SaleOrderDTO> listOrdersByUser(Long userId, SaleOrdersQuery saleOrdersQuery);
    
    // 取消订单
    AppResult cancelOrder(Long userId, Long orderId);
}
