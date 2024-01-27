package com.harris.app.service.cache;

import com.harris.app.model.cache.StockCache;
import com.harris.domain.model.StockDeduction;

public interface StockCacheService {
    StockCache getStockCache(Long userId, Long itemId);

    boolean alignStock(Long itemId);

    boolean deductStock(StockDeduction stockDeduction);

    boolean revertStock(StockDeduction stockDeduction);
}
