package com.harris.app.service.cache;

import com.harris.app.model.cache.ItemStockCache;
import com.harris.domain.model.StockDeduction;

public interface ItemStockCacheService {
    ItemStockCache getAvailableItemStock(Long userId, Long itemId);

    boolean alignItemStocks(Long itemId);

    boolean decreaseItemStock(StockDeduction stockDeduction);

    boolean increaseItemStock(StockDeduction stockDeduction);
}
