package com.harris.domain.model.event;

import com.alibaba.cola.event.DomainEventI;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.model.enums.SaleItemEventType;
import lombok.Data;

@Data
public class SaleItemEvent implements DomainEventI {
    private SaleItem saleItem;
    private SaleItemEventType saleItemEventType;
    
    public Long getItemId() {
        if (saleItem == null) return null;
        return saleItem.getId();
    }
    
    public Long getActivityId() {
        if (saleItem == null) return null;
        return saleItem.getActivityId();
    }
}
