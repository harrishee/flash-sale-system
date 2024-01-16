package com.harris.app.service.app;

import com.harris.app.model.PlaceOrderTask;
import com.harris.app.model.enums.OrderTaskStatus;
import com.harris.app.model.result.OrderTaskSubmitResult;

public interface PlaceOrderTaskService {
    OrderTaskStatus getTaskStatus(String placeOrderTaskId);

    OrderTaskSubmitResult submit(PlaceOrderTask placeOrderTask);

    void updateTaskHandleResult(String placeOrderTaskId, boolean result);
}
