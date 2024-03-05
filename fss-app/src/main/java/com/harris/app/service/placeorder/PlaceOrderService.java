package com.harris.app.service.placeorder;

import com.harris.app.model.command.PlaceOrderCommand;
import com.harris.app.model.result.PlaceOrderResult;

public interface PlaceOrderService {
    PlaceOrderResult doPlaceOrder(Long userId, PlaceOrderCommand placeOrderCommand);
}
