package com.harris.app.service.app;

import com.harris.app.model.command.PublishActivityCommand;
import com.harris.app.model.dto.SaleActivityDTO;
import com.harris.app.model.query.SaleActivitiesQuery;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;

public interface SaleActivityAppService {
    // 获取单个活动详情（包含版本号）
    AppSingleResult<SaleActivityDTO> getActivity(Long userId, Long activityId, Long version);
    
    // 获取活动列表
    AppMultiResult<SaleActivityDTO> listActivities(Long userId, SaleActivitiesQuery saleActivitiesQuery);
    
    // 发布活动
    AppResult publishActivity(Long userId, PublishActivityCommand publishActivityCommand);
    
    // 修改活动
    AppResult modifyActivity(Long userId, Long activityId, PublishActivityCommand publishActivityCommand);
    
    // 上线活动
    AppResult onlineActivity(Long userId, Long activityId);
    
    // 下线活动
    AppResult offlineActivity(Long userId, Long activityId);
    
    // 检查是否允许下单
    boolean isPlaceOrderAllowed(Long activityId);
}
