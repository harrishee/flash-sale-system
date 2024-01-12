package com.harris.domain.service;

import com.harris.domain.model.StockDeduction;

public interface StockDomainService {
    boolean decreaseItemStock(StockDeduction stockDeduction);

    boolean increaseItemStock(StockDeduction stockDeduction);
}
