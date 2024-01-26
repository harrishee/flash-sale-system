package com.harris.domain.service;

import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleActivity;

public interface FssActivityDomainService {
    SaleActivity getActivity(Long activityId);

    PageResult<SaleActivity> getActivities(PageQueryCondition pageQueryCondition);

    void publishActivity(Long userId, SaleActivity saleActivity);

    void modifyActivity(Long userId, SaleActivity saleActivity);

    void onlineActivity(Long userId, Long activityId);

    void offlineActivity(Long userId, Long activityId);
}
