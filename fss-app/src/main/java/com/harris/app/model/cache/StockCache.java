package com.harris.app.model.cache;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StockCache {
    private Integer availableStock;
    private boolean success;
    private boolean later;
    private boolean exist;

    public StockCache with(Integer availableStock) {
        this.availableStock = availableStock;
        this.success = true;
        this.exist = true;
        return this;
    }

    public StockCache tryLater() {
        this.success = false;
        this.later = true;
        return this;
    }
}
