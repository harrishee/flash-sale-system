package com.harris.app.service.impl;

import com.harris.app.model.command.PlaceOrderCommand;
import com.harris.app.model.dto.FlashOrderDTO;
import com.harris.app.model.query.FlashOrdersQuery;
import com.harris.app.model.result.*;
import com.harris.app.service.FlashOrderAppService;

public class FlashOrderAppServiceImpl implements FlashOrderAppService {
    @Override
    public AppSingleResult<PlaceOrderResult> placeOrder(Long userId, PlaceOrderCommand placeOrderCommand) {
        return null;
    }

    @Override
    public AppSingleResult<OrderTaskResult> getPlaceOrderTaskResult(Long userId, Long itemId, String placeOrderTaskId) {
        return null;
    }

    @Override
    public AppMultiResult<FlashOrderDTO> getOrdersByUser(Long userId, FlashOrdersQuery flashOrdersQuery) {
        return null;
    }

    @Override
    public AppResult cancelOrder(Long userId, Long orderId) {
        return null;
    }
}
