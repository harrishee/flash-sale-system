package com.harris.app.service;

import com.harris.app.model.command.PlaceOrderCommand;
import com.harris.app.model.dto.FlashOrderDTO;
import com.harris.app.model.query.FlashOrdersQuery;
import com.harris.app.model.result.*;

public interface FlashOrderAppService {
    AppSingleResult<PlaceOrderResult> placeOrder(Long userId, PlaceOrderCommand placeOrderCommand);

    AppSingleResult<OrderTaskResult> getPlaceOrderTaskResult(Long userId, Long itemId, String placeOrderTaskId);

    AppMultiResult<FlashOrderDTO> getOrdersByUser(Long userId, FlashOrdersQuery flashOrdersQuery);

    AppResult cancelOrder(Long userId, Long orderId);
}
