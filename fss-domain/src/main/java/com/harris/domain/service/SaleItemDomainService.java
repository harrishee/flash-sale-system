package com.harris.domain.service;

import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleItem;

public interface SaleItemDomainService {
    // 根据 商品ID 查询 商品
    SaleItem getItem(Long itemId);
    
    // 根据 页面查询条件 查询 商品列表 并返回分页结果
    PageResult<SaleItem> getItems(PageQuery pageQuery);
    
    // 根据 商品 发布 商品
    void publishItem(SaleItem saleItem);
    
    // 根据 商品ID 上线 商品
    void onlineItem(Long itemId);
    
    // 根据 商品ID 下线 商品
    void offlineItem(Long itemId);
}
