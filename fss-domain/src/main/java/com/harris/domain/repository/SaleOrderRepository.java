package com.harris.domain.repository;

import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleOrder;

import java.util.List;
import java.util.Optional;

public interface SaleOrderRepository {
    // 根据 订单ID 查询 订单
    Optional<SaleOrder> findOrderById(Long orderId);
    
    // 根据 页面查询条件 查询 订单列表
    List<SaleOrder> findAllOrderByCondition(PageQuery pageQuery);
    
    // 根据 页面查询条件 查询 订单数量
    int countOrdersByCondition(PageQuery pageQuery);
    
    // 保存 订单
    boolean saveOrder(SaleOrder saleOrder);
    
    // 更新 订单状态
    boolean updateStatus(SaleOrder saleOrder);
}
