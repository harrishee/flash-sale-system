package com.harris.app.service.app;

import com.harris.app.model.command.FlashPlaceOrderCommand;
import com.harris.app.model.result.PlaceOrderResult;

public interface PlaceOrderService {
    PlaceOrderResult doPlaceOrder(Long userId, FlashPlaceOrderCommand placeOrderCommand);
}
