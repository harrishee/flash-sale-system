package com.harris.domain.repository;

import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleOrder;

import java.util.List;
import java.util.Optional;

public interface SaleOrderRepository {
    Optional<SaleOrder> findOrderById(Long orderId);
    
    List<SaleOrder> findAllOrderByCondition(PageQuery pageQuery);
    
    int countAllOrderByCondition(PageQuery pageQuery);
    
    boolean saveOrder(SaleOrder saleOrder);
    
    boolean updateStatus(SaleOrder saleOrder);
}
