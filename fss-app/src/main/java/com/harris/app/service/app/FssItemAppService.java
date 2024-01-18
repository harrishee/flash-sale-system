package com.harris.app.service.app;

import com.harris.app.model.command.FlashItemPublishCommand;
import com.harris.app.model.dto.SaleItemDTO;
import com.harris.app.model.query.FlashItemsQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;

public interface FlashItemAppService {
    AppSingleResult<SaleItemDTO> getFlashItem(Long itemId);

    AppSingleResult<SaleItemDTO> getFlashItem(Long userId, Long activityId, Long itemId, Long version);

    AppMultiResult<SaleItemDTO> getFlashItems(Long userId, Long activityId, FlashItemsQuery flashItemsQuery);

    AppResult publishFlashItem(Long userId, Long activityId, FlashItemPublishCommand flashItemPublishCommand);

    AppResult onlineFlashItem(Long userId, Long activityId, Long itemId);

    AppResult offlineFlashItem(Long userId, Long activityId, Long itemId);

    boolean isPlaceOrderAllowed(Long itemId);
}
