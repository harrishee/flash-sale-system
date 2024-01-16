package com.harris.app.service.main;

import com.harris.app.model.command.FlashItemPublishCommand;
import com.harris.app.model.dto.FlashItemDTO;
import com.harris.app.model.query.FlashItemsQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;

public interface FlashItemAppService {
    AppSingleResult<FlashItemDTO> getFlashItem(Long itemId);

    AppSingleResult<FlashItemDTO> getFlashItem(Long userId, Long activityId, Long itemId, Long version);

    AppMultiResult<FlashItemDTO> getFlashItems(Long userId, Long activityId, FlashItemsQuery flashItemsQuery);

    AppResult publishFlashItem(Long userId, Long activityId, FlashItemPublishCommand flashItemPublishCommand);

    AppResult onlineFlashItem(Long userId, Long activityId, Long itemId);

    AppResult offlineFlashItem(Long userId, Long activityId, Long itemId);

    boolean isPlaceOrderAllowed(Long itemId);
}
