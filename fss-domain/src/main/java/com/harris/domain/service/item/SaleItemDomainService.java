package com.harris.domain.service.item;

import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleItem;

public interface SaleItemDomainService {
    SaleItem getItem(Long itemId);
    
    PageResult<SaleItem> getItems(PageQuery pageQuery);
    
    void publishItem(SaleItem saleItem);
    
    void onlineItem(Long itemId);
    
    void offlineItem(Long itemId);
}
