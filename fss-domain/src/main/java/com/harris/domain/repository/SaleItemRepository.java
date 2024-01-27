package com.harris.domain.repository;

import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleItem;

import java.util.List;
import java.util.Optional;

public interface SaleItemRepository {
    /**
     * Find a sale item by its ID.
     *
     * @param itemId The item ID
     * @return Optional object of the sale item
     */
    Optional<SaleItem> findItemById(Long itemId);

    /**
     * Find sale items by the given condition.
     *
     * @param pageQuery The condition
     * @return List of sale items
     */
    List<SaleItem> findItemsByCondition(PageQuery pageQuery);

    /**
     * Count total sale items by the given condition.
     *
     * @param pageQuery The condition
     * @return The count of sale items
     */
    Integer countItemsByCondition(PageQuery pageQuery);

    /**
     * Saves a sale item, either by inserting or updating it.
     *
     * @param saleItem The sale item
     * @return The count of effected rows
     */
    int saveItem(SaleItem saleItem);

    /**
     * Deduct stock for a sale item.
     *
     * @param itemId   The item ID
     * @param quantity The quantity to deduct
     * @return Deducted result
     */
    boolean deductStockForItem(Long itemId, Integer quantity);

    /**
     * Revert stock for a sale item.
     *
     * @param itemId   The item ID
     * @param quantity The quantity to revert
     * @return Reverted result
     */
    boolean revertStockForItem(Long itemId, Integer quantity);
}
