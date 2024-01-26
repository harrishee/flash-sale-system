package com.harris.domain.service;

import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleOrder;

public interface SaleOrderDomainService {
    /**
     * Retrieve order by user id and order id.
     *
     * @param userId  The user id
     * @param orderId The order id
     * @return The SaleOrder
     */
    SaleOrder getOrder(Long userId, Long orderId);

    /**
     * Retrieve a paginated list of SaleOrder by the given condition.
     *
     * @param userId             The user id
     * @param pageQueryCondition The page query condition
     * @return The PageResult with SaleOrders and the total count
     */
    PageResult<SaleOrder> getOrdersByUserId(Long userId, PageQueryCondition pageQueryCondition);

    /**
     * save a SaleOrder and publish a place order event.
     *
     * @param userId    The user id
     * @param saleOrder The SaleOrder
     * @return save result
     */
    boolean placeOrder(Long userId, SaleOrder saleOrder);

    /**
     * update the order status and publish a cancel order event.
     *
     * @param userId  The user id
     * @param orderId The order id
     * @return cancel result
     */
    boolean cancelOrder(Long userId, Long orderId);
}
