package com.harris.app.model.cache;

import com.harris.domain.model.entity.SaleItem;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class SaleItemsCache {
    private List<SaleItem> saleItems;
    private Integer total;
    private Long version;
    private boolean later;
    private boolean exist;
    private boolean empty;
    
    public SaleItemsCache with(List<SaleItem> saleItems) {
        this.saleItems = saleItems;
        this.exist = true;
        return this;
    }
    
    public SaleItemsCache tryLater() {
        this.later = true;
        return this;
    }
    
    public SaleItemsCache empty() {
        this.saleItems = new ArrayList<>();
        this.total = 0;
        this.empty = true;
        return this;
    }
}
