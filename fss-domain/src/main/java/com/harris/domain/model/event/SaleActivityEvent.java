package com.harris.domain.model.event;

import com.alibaba.cola.event.DomainEventI;
import com.harris.domain.model.entity.SaleActivity;
import com.harris.domain.model.enums.SaleActivityEventType;
import lombok.Data;

@Data
public class SaleActivityEvent implements DomainEventI {
    private SaleActivity saleActivity;
    private SaleActivityEventType saleActivityEventType;
    
    public Long getId() {
        if (saleActivity == null) return null;
        return saleActivity.getId();
    }
}
