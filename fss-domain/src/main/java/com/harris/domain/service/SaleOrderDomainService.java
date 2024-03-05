package com.harris.domain.service;

import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleOrder;

public interface SaleOrderDomainService {
    SaleOrder getOrder(Long userId, Long orderId);
    
    PageResult<SaleOrder> getOrders(Long userId, PageQuery pageQuery);
    
    boolean createOrder(Long userId, SaleOrder saleOrder);
    
    boolean cancelOrder(Long userId, Long orderId);
}
