package com.harris.app.service.stock;

import com.harris.app.model.cache.StockCache;
import com.harris.domain.model.StockDeduction;

public interface StockCacheService {
    StockCache getStockCache(Long userId, Long itemId);

    boolean syncCachedStockToDB(Long itemId);

    boolean deductStock(StockDeduction stockDeduction);

    boolean revertStock(StockDeduction stockDeduction);
}
