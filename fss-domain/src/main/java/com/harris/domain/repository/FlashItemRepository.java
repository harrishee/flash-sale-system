package com.harris.domain.repository;

import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashItem;

import java.util.List;
import java.util.Optional;

public interface FlashItemRepository {
    int saveItem(FlashItem flashItem);

    Optional<FlashItem> findItemById(Long itemId);

    List<FlashItem> findItemsByCondition(PagesQueryCondition pagesQueryCondition);

    Integer countItemsByCondition(PagesQueryCondition pagesQueryCondition);

    boolean decreaseStockForItem(Long itemId, Integer quantity);

    boolean increaseStockForItem(Long itemId, Integer quantity);
}
