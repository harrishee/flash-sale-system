package com.harris.domain.event.flashItem;

import com.alibaba.cola.event.DomainEventI;
import com.harris.domain.model.entity.FlashItem;
import lombok.Data;

@Data
public class FlashItemEvent implements DomainEventI {
    private FlashItem flashItem;
    private FlashItemEventType flashItemEventType;

    public Long getId() {
        if (flashItem == null) {
            return null;
        }
        return flashItem.getId();
    }

    public Long getFlashActivityId() {
        if (flashItem == null) {
            return null;
        }
        return flashItem.getActivityId();
    }
}
