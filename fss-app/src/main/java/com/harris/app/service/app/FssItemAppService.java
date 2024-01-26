package com.harris.app.service.app;

import com.harris.app.model.command.PublishItemCommand;
import com.harris.app.model.dto.SaleItemDTO;
import com.harris.app.model.query.SaleItemsQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;

public interface FssItemAppService {
    AppSingleResult<SaleItemDTO> getItem(Long itemId);

    AppSingleResult<SaleItemDTO> getItem(Long userId, Long activityId, Long itemId, Long version);

    AppMultiResult<SaleItemDTO> listItems(Long userId, Long activityId, SaleItemsQuery saleItemsQuery);

    AppResult publishItem(Long userId, Long activityId, PublishItemCommand publishItemCommand);

    AppResult onlineItem(Long userId, Long activityId, Long itemId);

    AppResult offlineItem(Long userId, Long activityId, Long itemId);

    boolean isPlaceOrderAllowed(Long itemId);
}
