package com.harris.app.service.app;

import com.harris.app.model.command.FlashPlaceOrderCommand;
import com.harris.app.model.dto.SaleOrderDTO;
import com.harris.app.model.query.FlashOrdersQuery;
import com.harris.app.model.result.*;

public interface FlashOrderAppService {
    AppSingleResult<PlaceOrderResult> placeOrder(Long userId, FlashPlaceOrderCommand flashPlaceOrderCommand);

    AppSingleResult<OrderTaskHandleResult> getPlaceOrderTaskResult(Long userId, Long itemId, String placeOrderTaskId);

    AppMultiResult<SaleOrderDTO> getOrdersByUser(Long userId, FlashOrdersQuery flashOrdersQuery);

    AppResult cancelOrder(Long userId, Long orderId);
}
