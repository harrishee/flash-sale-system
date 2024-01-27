package com.harris.app.service.app;

import com.harris.app.model.PlaceOrderTask;
import com.harris.app.model.enums.PlaceOrderTaskStatus;
import com.harris.app.model.result.OrderSubmitResult;

public interface PlaceOrderTaskService {
    PlaceOrderTaskStatus getStatus(String placeOrderTaskId);

    OrderSubmitResult submit(PlaceOrderTask placeOrderTask);

    void updateHandleResult(String placeOrderTaskId, boolean result);
}
