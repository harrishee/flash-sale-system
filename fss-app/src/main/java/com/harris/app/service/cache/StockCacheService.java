package com.harris.app.service.cache;

import com.harris.app.model.cache.ItemStockCache;
import com.harris.domain.model.StockDeduction;

public interface StockCacheService {
    ItemStockCache getAvailableStock(Long userId, Long itemId);

    boolean alignItemStocks(Long itemId);

    boolean deductStock(StockDeduction stockDeduction);

    boolean revertStock(StockDeduction stockDeduction);
}
