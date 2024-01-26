package com.harris.app.service.app;

import com.harris.app.model.command.PurchaseCommand;
import com.harris.app.model.result.PurchaseResult;

public interface PlaceOrderService {
    /**
     * Interface for processing order placement. Implementations of this service can vary
     * based on the order handling strategy
     * <p>
     * StandardPlaceOrderService: Implements a synchronous approach to directly handle
     * the order placement. It validates the inputs, checks whether the item and activity
     * allow for placing an order, and then processes the order deduction and creation
     * in a synchronous manner
     * <p>
     * QueuedPlaceOrderService: Utilizes an asynchronous, queued approach for handling
     * orders. It generates a PlaceOrderTask and submits it to a queue for processing.
     * The actual order placement logic is handled within the queued task, making it suitable
     * for high-throughput and high-concurrency scenarios
     */
    PurchaseResult doPlaceOrder(Long userId, PurchaseCommand purchaseCommand);
}
