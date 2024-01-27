package com.harris.app.service.app;

import com.harris.app.model.command.PlaceOrderCommand;
import com.harris.app.model.dto.SaleOrderDTO;
import com.harris.app.model.query.SaleOrdersQuery;
import com.harris.app.model.result.*;

public interface SaleOrderAppService {
    AppSingleResult<PlaceOrderResult> placeOrder(Long userId, PlaceOrderCommand placeOrderCommand);

    AppSingleResult<OrderHandleResult> getOrder(Long userId, Long itemId, String placeOrderTaskId);

    AppMultiResult<SaleOrderDTO> listOrdersByUser(Long userId, SaleOrdersQuery saleOrdersQuery);

    AppResult cancelOrder(Long userId, Long orderId);
}
