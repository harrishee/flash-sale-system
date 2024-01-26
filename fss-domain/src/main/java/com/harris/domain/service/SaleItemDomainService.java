package com.harris.domain.service;

import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleItem;

public interface SaleItemDomainService {
    /**
     * Retrieve item by id.
     *
     * @param itemId The item id
     * @return The SaleItem
     */
    SaleItem getItem(Long itemId);

    /**
     * Retrieve a paginated list of SaleItem by the given condition.
     *
     * @param pageQueryCondition The page query condition
     * @return The PageResult with SaleItems and the total count
     */
    PageResult<SaleItem> getItems(PageQueryCondition pageQueryCondition);

    /**
     * Publish a SaleItem and publish a publish item event.
     *
     * @param saleItem The SaleItem
     */
    void publishItem(SaleItem saleItem);

    /**
     * Online a SaleItem and publish a online item event.
     *
     * @param itemId The item ID
     */
    void onlineItem(Long itemId);

    /**
     * Offline a SaleItem and publish a offline item event.
     *
     * @param itemId The item ID
     */
    void offlineItem(Long itemId);
}
