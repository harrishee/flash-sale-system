package com.harris.app.service.app;

import com.harris.app.model.PlaceOrderTask;
import com.harris.app.model.PlaceOrderTaskStatus;
import com.harris.app.model.result.OrderSubmitResult;

public interface PlaceOrderTaskService {
    PlaceOrderTaskStatus getStatus(String placeOrderTaskId);

    OrderSubmitResult submit(PlaceOrderTask placeOrderTask);

    void updateTaskHandleResult(String placeOrderTaskId, boolean result);
}
