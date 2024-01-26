package com.harris.domain.event;

import com.alibaba.cola.event.DomainEventI;
import com.alibaba.cola.event.EventBusI;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class LocalDomainEventPublisher implements DomainEventPublisher {
    @Resource
    private EventBusI eventBus;

    @Override
    public void publish(DomainEventI domainEvent) {
        // Publish the domain event to the event bus
        eventBus.fire((domainEvent));
    }
}
