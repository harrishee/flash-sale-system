package com.harris.domain.repository;

import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleItem;

import java.util.List;
import java.util.Optional;

public interface SaleItemRepository {
    Optional<SaleItem> findItemById(Long itemId);
    
    List<SaleItem> findAllItemByCondition(PageQuery pageQuery);
    
    Integer countAllItemByCondition(PageQuery pageQuery);
    
    int saveItem(SaleItem saleItem);
    
    boolean deductStockForItem(Long itemId, Integer quantity);
    
    boolean revertStockForItem(Long itemId, Integer quantity);
}
