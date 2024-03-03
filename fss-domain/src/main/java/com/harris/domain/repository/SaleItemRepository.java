package com.harris.domain.repository;

import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleItem;

import java.util.List;
import java.util.Optional;

public interface SaleItemRepository {
    // 根据 商品ID 查询 商品
    Optional<SaleItem> findItemById(Long itemId);
    
    // 根据 页面查询条件 查询 商品列表
    List<SaleItem> findAllItemByCondition(PageQuery pageQuery);
    
    // 根据 页面查询条件 查询 商品数量
    Integer countAllItemByCondition(PageQuery pageQuery);
    
    // 保存 商品
    int saveItem(SaleItem saleItem);
    
    // 根据 商品ID 和 数量 扣减 库存
    boolean deductStockForItem(Long itemId, Integer quantity);
    
    // 根据 商品ID 和 数量 回滚 库存
    boolean revertStockForItem(Long itemId, Integer quantity);
}
