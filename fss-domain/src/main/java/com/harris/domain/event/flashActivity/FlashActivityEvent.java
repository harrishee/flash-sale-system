package com.harris.domain.event.flashActivity;

import com.alibaba.cola.event.DomainEventI;
import com.harris.domain.model.entity.SaleActivity;
import lombok.Data;

@Data
public class FlashActivityEvent implements DomainEventI {
    private SaleActivity saleActivity;
    private FlashActivityEventType flashActivityEventType;

    public Long getId() {
        if (saleActivity == null) {
            return null;
        }
        return saleActivity.getId();
    }
}
