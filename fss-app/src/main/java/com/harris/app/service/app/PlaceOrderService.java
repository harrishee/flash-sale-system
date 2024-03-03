package com.harris.app.service.app;

import com.harris.app.model.command.PlaceOrderCommand;
import com.harris.app.model.result.PlaceOrderResult;

public interface PlaceOrderService {
    // 下单操作
    PlaceOrderResult doPlaceOrder(Long userId, PlaceOrderCommand placeOrderCommand);
}
