package com.harris.domain.service;

import com.harris.domain.model.StockDeduction;

/**
 * Stock deduction domain service.
 * There are two implementations of this service:
 * 1. StandardStockDomainService: Handles stock deduction in a standard way.
 * 2. BucketStockDomainService: Manages stock based on a bucketing system.
 */
public interface StockDomainService {
    /**
     * Deduct stock.
     *
     * @param stockDeduction stock deduction info
     * @return deduct result
     */
    boolean deductStock(StockDeduction stockDeduction);

    /**
     * Revert stock.
     *
     * @param stockDeduction stock deduction info
     * @return revert result
     */
    boolean revertStock(StockDeduction stockDeduction);
}
