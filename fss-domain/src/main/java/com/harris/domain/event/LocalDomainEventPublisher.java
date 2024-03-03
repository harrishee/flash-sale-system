package com.harris.domain.event;

import com.alibaba.cola.event.DomainEventI;
import com.alibaba.cola.event.EventBusI;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class LocalDomainEventPublisher implements DomainEventPublisher {
    @Resource
    private EventBusI eventBus; // 事件总线，用于管理事件的发布和订阅
    
    @Override
    public void publish(DomainEventI domainEvent) {
        // eventBus.fire() 方法用于发布事件
        eventBus.fire((domainEvent));
    }
}
