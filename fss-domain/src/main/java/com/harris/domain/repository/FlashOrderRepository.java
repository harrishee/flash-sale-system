package com.harris.domain.repository;

import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashOrder;

import java.util.List;
import java.util.Optional;

public interface FlashOrderRepository {
    boolean saveOrder(FlashOrder flashOrder);

    boolean updateStatusForOrder(FlashOrder flashOrder);

    Optional<FlashOrder> findOrderById(Long orderId);

    List<FlashOrder> findOrdersByCondition(PagesQueryCondition pagesQueryCondition);

    int countOrdersByCondition(PagesQueryCondition buildParams);
}
