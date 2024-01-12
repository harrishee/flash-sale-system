package com.harris.domain.event.flashOrder;

import com.alibaba.cola.event.DomainEventI;
import lombok.Data;

@Data
public class FlashOrderEvent implements DomainEventI {
    private Long orderId;
    private FlashOrderEventType flashOrderEventType;
}
