package com.harris.domain.event;

import com.alibaba.cola.event.DomainEventI;

public interface DomainEventPublisher {
    void publish(DomainEventI domainEvent);
}
