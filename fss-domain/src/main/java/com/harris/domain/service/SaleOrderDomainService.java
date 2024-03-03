package com.harris.domain.service;

import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleOrder;

public interface SaleOrderDomainService {
    // 根据 用户ID 和 订单ID 查询 订单
    SaleOrder getOrder(Long userId, Long orderId);
    
    // 根据 用户ID 和 页面查询条件 查询 订单列表 并返回分页结果
    PageResult<SaleOrder> getOrders(Long userId, PageQuery pageQuery);
    
    // 根据 用户ID 和 订单 下单
    boolean placeOrder(Long userId, SaleOrder saleOrder);
    
    // 根据 用户ID 和 订单ID 取消 订单
    boolean cancelOrder(Long userId, Long orderId);
}
