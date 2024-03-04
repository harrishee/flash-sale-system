package com.harris.domain.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StockDeduction {
    private Long userId;
    private Long itemId;
    private Integer quantity;
    private Integer serialNo;
    
    public boolean invalidParams() {
        return userId == null || itemId == null || quantity == null || quantity <= 0;
    }
}
