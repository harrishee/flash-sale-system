package com.harris.app.service.app;

import com.harris.app.model.command.PublishItemCommand;
import com.harris.app.model.dto.SaleItemDTO;
import com.harris.app.model.query.SaleItemsQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;

public interface SaleItemAppService {
    // 获取单个抢购品详情
    AppSingleResult<SaleItemDTO> getItem(Long itemId);
    
    // 获取单个抢购品详情（包含版本号）
    AppSingleResult<SaleItemDTO> getItem(Long userId, Long activityId, Long itemId, Long version);
    
    // 获取抢购品列表
    AppMultiResult<SaleItemDTO> listItems(Long userId, Long activityId, SaleItemsQuery saleItemsQuery);
    
    // 发布抢购品
    AppResult publishItem(Long userId, Long activityId, PublishItemCommand publishItemCommand);
    
    // 上线抢购品
    AppResult onlineItem(Long userId, Long activityId, Long itemId);
    
    // 下线抢购品
    AppResult offlineItem(Long userId, Long activityId, Long itemId);
    
    // 检查是否允许下单
    boolean isPlaceOrderAllowed(Long itemId);
}
