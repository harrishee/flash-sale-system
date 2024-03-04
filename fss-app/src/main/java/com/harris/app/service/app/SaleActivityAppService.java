package com.harris.app.service.app;

import com.harris.app.model.command.PublishActivityCommand;
import com.harris.app.model.dto.SaleActivityDTO;
import com.harris.app.model.query.SaleActivitiesQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;

public interface SaleActivityAppService {
    AppSingleResult<SaleActivityDTO> getActivity(Long userId, Long activityId, Long version);
    
    AppMultiResult<SaleActivityDTO> listActivities(Long userId, SaleActivitiesQuery saleActivitiesQuery);
    
    AppResult publishActivity(Long userId, PublishActivityCommand publishActivityCommand);
    
    AppResult modifyActivity(Long userId, Long activityId, PublishActivityCommand publishActivityCommand);
    
    AppResult onlineActivity(Long userId, Long activityId);
    
    AppResult offlineActivity(Long userId, Long activityId);
    
    boolean isPlaceOrderAllowed(Long activityId);
}
