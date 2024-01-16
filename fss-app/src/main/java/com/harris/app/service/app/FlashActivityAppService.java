package com.harris.app.service.app;

import com.harris.app.model.command.FlashActivityPublishCommand;
import com.harris.app.model.dto.FlashActivityDTO;
import com.harris.app.model.query.FlashActivitiesQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;

public interface FlashActivityAppService {
    AppSingleResult<FlashActivityDTO> getFlashActivity(Long userId, Long activityId, Long version);

    AppMultiResult<FlashActivityDTO> getFlashActivities(Long userId, FlashActivitiesQuery flashActivitiesQuery);

    AppResult publishFlashActivity(Long userId, FlashActivityPublishCommand flashActivityPublishCommand);

    AppResult modifyFlashActivity(Long userId, Long activityId, FlashActivityPublishCommand flashActivityPublishCommand);

    AppResult onlineFlashActivity(Long userId, Long activityId);

    AppResult offlineFlashActivity(Long userId, Long activityId);

    boolean isPlaceOrderAllowed(Long activityId);
}
