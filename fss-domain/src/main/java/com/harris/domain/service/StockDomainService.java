package com.harris.domain.service;

import com.harris.domain.model.StockDeduction;

public interface StockDomainService {
    boolean deductStock(StockDeduction stockDeduction);
    
    boolean revertStock(StockDeduction stockDeduction);
}
