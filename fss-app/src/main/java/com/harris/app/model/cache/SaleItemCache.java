package com.harris.app.model.cache;

import com.harris.domain.model.entity.SaleItem;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SaleItemCache {
    private SaleItem saleItem;
    private Long version;
    private boolean later;
    private boolean exist;
    
    public SaleItemCache with(SaleItem saleItem) {
        this.saleItem = saleItem;
        this.exist = true;
        return this;
    }
    
    public SaleItemCache withVersion(Long version) {
        this.version = version;
        return this;
    }
    
    public SaleItemCache tryLater() {
        this.later = true;
        return this;
    }
    
    public SaleItemCache notExist() {
        this.exist = false;
        return this;
    }
}
