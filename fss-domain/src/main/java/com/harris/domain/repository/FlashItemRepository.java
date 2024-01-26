package com.harris.domain.repository;

import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleItem;

import java.util.List;
import java.util.Optional;

public interface FlashItemRepository {
    Optional<SaleItem> findItemById(Long itemId);

    List<SaleItem> findItemsByCondition(PageQueryCondition pageQueryCondition);

    Integer countItemsByCondition(PageQueryCondition pageQueryCondition);

    int saveItem(SaleItem saleItem);

    boolean deductStockForItem(Long itemId, Integer quantity);

    boolean revertStockForItem(Long itemId, Integer quantity);
}
