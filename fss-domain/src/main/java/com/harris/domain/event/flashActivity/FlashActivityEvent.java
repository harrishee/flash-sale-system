package com.harris.domain.event.flashActivity;

import com.alibaba.cola.event.DomainEventI;
import com.harris.domain.model.entity.FlashActivity;
import lombok.Data;

@Data
public class FlashActivityEvent implements DomainEventI {
    private FlashActivity flashActivity;
    private FlashActivityEventType flashActivityEventType;
}
