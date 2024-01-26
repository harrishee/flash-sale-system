package com.harris.domain.service;

import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleOrder;

public interface FssOrderDomainService {
    SaleOrder getOrder(Long userId, Long orderId);

    PageResult<SaleOrder> getOrdersByUserId(Long userId, PageQueryCondition pageQueryCondition);

    boolean placeOrder(Long userId, SaleOrder saleOrder);

    boolean cancelOrder(Long userId, Long orderId);
}
