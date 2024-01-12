package com.harris.domain.service;

import com.harris.domain.model.PageResult;
import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashActivity;

public interface FlashActivityDomainService {
    FlashActivity getActivity(Long activityId);

    PageResult<FlashActivity> getActivities(PagesQueryCondition pagesQueryCondition);

    void publishActivity(Long userId, FlashActivity flashActivity);

    void modifyActivity(Long userId, FlashActivity flashActivity);

    void onlineActivity(Long userId, Long activityId);

    void offlineActivity(Long userId, Long activityId);
}
