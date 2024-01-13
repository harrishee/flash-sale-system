package com.harris.app.service.impl;

import com.harris.app.model.command.FlashActivityPublishCommand;
import com.harris.app.model.dto.FlashActivityDTO;
import com.harris.app.model.query.FlashActivitiesQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;
import com.harris.app.service.FlashActivityAppService;

public class FlashActivityAppServiceImpl implements FlashActivityAppService {
    @Override
    public AppSingleResult<FlashActivityDTO> getFlashActivity(Long userId, Long activityId, Long version) {
        return null;
    }

    @Override
    public AppMultiResult<FlashActivityDTO> getFlashActivities(Long userId, FlashActivitiesQuery flashActivitiesQuery) {
        return null;
    }

    @Override
    public AppResult publishFlashActivity(Long userId, FlashActivityPublishCommand flashActivityPublishCommand) {
        return null;
    }

    @Override
    public AppResult modifyFlashActivity(Long userId, Long activityId, FlashActivityPublishCommand flashActivityPublishCommand) {
        return null;
    }

    @Override
    public AppResult onlineFlashActivity(Long userId, Long activityId) {
        return null;
    }

    @Override
    public AppResult offlineFlashActivity(Long userId, Long activityId) {
        return null;
    }

    @Override
    public boolean isPlaceOrderAllowed(Long activityId) {
        return false;
    }
}
