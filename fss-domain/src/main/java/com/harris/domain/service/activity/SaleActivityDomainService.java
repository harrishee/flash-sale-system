package com.harris.domain.service.activity;

import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleActivity;

public interface SaleActivityDomainService {
    SaleActivity getActivity(Long activityId);
    
    PageResult<SaleActivity> getActivities(PageQuery pageQuery);
    
    void publishActivity(Long userId, SaleActivity saleActivity);
    
    void modifyActivity(Long userId, SaleActivity saleActivity);
    
    void onlineActivity(Long userId, Long activityId);
    
    void offlineActivity(Long userId, Long activityId);
}
