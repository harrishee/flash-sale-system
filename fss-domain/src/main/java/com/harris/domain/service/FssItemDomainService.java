package com.harris.domain.service;

import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleItem;

public interface FssItemDomainService {
    SaleItem getItem(Long itemId);

    PageResult<SaleItem> getItems(PageQueryCondition pageQueryCondition);

    void publishItem(SaleItem saleItem);

    void onlineItem(Long itemId);

    void offlineItem(Long itemId);
}
