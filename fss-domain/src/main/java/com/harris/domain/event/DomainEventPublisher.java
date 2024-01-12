package com.harris.domain.event;

import com.alibaba.cola.event.DomainEventI;

public interface DomainEventPublisher {
    public void publish(DomainEventI domainEvent);
}
