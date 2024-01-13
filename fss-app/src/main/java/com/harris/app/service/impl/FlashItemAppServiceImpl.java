package com.harris.app.service.impl;

import com.harris.app.model.command.FlashItemPublishCommand;
import com.harris.app.model.dto.FlashItemDTO;
import com.harris.app.model.query.FlashItemsQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.service.FlashItemAppService;

public class FlashItemAppServiceImpl implements FlashItemAppService {
    @Override
    public AppSingleResult<FlashItemDTO> getFlashItem(Long itemId) {
        return null;
    }

    @Override
    public AppSingleResult<FlashItemDTO> getFlashItem(Long userId, Long activityId, Long itemId, Long version) {
        return null;
    }

    @Override
    public AppMultiResult<FlashItemDTO> getFlashItems(Long userId, Long activityId, FlashItemsQuery flashItemsQuery) {
        return null;
    }

    @Override
    public AppResult publishFlashItem(Long userId, Long activityId, FlashItemPublishCommand flashItemPublishCommand) {
        return null;
    }

    @Override
    public AppResult onlineFlashItem(Long userId, Long activityId, Long itemId) {
        return null;
    }

    @Override
    public AppResult offlineFlashItem(Long userId, Long activityId, Long itemId) {
        return null;
    }

    @Override
    public boolean isPlaceOrderAllowed(Long itemId) {
        return false;
    }
}
