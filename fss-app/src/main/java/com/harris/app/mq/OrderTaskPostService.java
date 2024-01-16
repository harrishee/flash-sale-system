package com.harris.app.mq;

import com.harris.app.model.PlaceOrderTask;

public interface OrderTaskPostService {
    boolean post(PlaceOrderTask placeOrderTask);
}
