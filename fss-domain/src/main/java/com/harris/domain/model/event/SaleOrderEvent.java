package com.harris.domain.model.event;

import com.alibaba.cola.event.DomainEventI;
import com.harris.domain.model.enums.SaleOrderEventType;
import lombok.Data;

@Data
public class SaleOrderEvent implements DomainEventI {
    private Long orderId;
    private SaleOrderEventType saleOrderEventType;
}
