package com.harris.domain.repository;

import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleOrder;

import java.util.List;
import java.util.Optional;

public interface FlashOrderRepository {
    Optional<SaleOrder> findOrderById(Long orderId);

    List<SaleOrder> findOrdersByCondition(PageQueryCondition pageQueryCondition);

    int countOrdersByCondition(PageQueryCondition buildParams);

    boolean saveOrder(SaleOrder saleOrder);

    boolean updateStatusForOrder(SaleOrder saleOrder);
}
