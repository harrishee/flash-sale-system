package com.harris.domain.service;

import com.harris.domain.model.PageResult;
import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashOrder;

public interface FlashOrderDomainService {
    FlashOrder getOrder(Long userId, Long orderId);

    PageResult<FlashOrder> getOrdersByUserId(Long userId, PagesQueryCondition pagesQueryCondition);

    boolean placeOrder(Long userId, FlashOrder flashOrder);

    boolean cancelOrder(Long userId, Long orderId);
}
