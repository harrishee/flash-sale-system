package com.harris.domain.event.flashItem;

import com.alibaba.cola.event.DomainEventI;
import com.harris.domain.model.entity.SaleItem;
import lombok.Data;

@Data
public class FlashItemEvent implements DomainEventI {
    private SaleItem saleItem;
    private FlashItemEventType flashItemEventType;

    public Long getId() {
        if (saleItem == null) {
            return null;
        }
        return saleItem.getId();
    }

    public Long getFlashActivityId() {
        if (saleItem == null) {
            return null;
        }
        return saleItem.getActivityId();
    }
}
