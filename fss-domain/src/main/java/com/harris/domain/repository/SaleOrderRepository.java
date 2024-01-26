package com.harris.domain.repository;

import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleOrder;

import java.util.List;
import java.util.Optional;

public interface SaleOrderRepository {
    /**
     * Find a sale order by its ID.
     *
     * @param orderId The order ID
     * @return Optional object of the sale order
     */
    Optional<SaleOrder> findOrderById(Long orderId);

    /**
     * Find sale orders by the given condition.
     *
     * @param pageQueryCondition The condition
     * @return List of sale orders
     */
    List<SaleOrder> findOrdersByCondition(PageQueryCondition pageQueryCondition);

    /**
     * Count total sale orders by the given condition.
     *
     * @param pageQueryCondition The condition
     * @return The count of sale orders
     */
    int countOrdersByCondition(PageQueryCondition pageQueryCondition);

    /**
     * Saves a new sale order to DB.
     *
     * @param saleOrder The sale order
     * @return The count of effected rows
     */
    boolean saveOrder(SaleOrder saleOrder);

    /**
     * Updates the status of a sale order.
     *
     * @param saleOrder The sale order
     * @return The count of effected rows
     */
    boolean updateStatus(SaleOrder saleOrder);
}
