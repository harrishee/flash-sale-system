package com.harris.domain.service;

import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleActivity;

public interface SaleActivityDomainService {
    // 根据 活动ID 查询 活动
    SaleActivity getActivity(Long activityId);
    
    // 根据 页面查询条件 查询 活动列表 并返回分页结果
    PageResult<SaleActivity> getActivities(PageQuery pageQuery);
    
    // 根据 用户ID 和 活动 发布 活动
    void publishActivity(Long userId, SaleActivity saleActivity);
    
    // 根据 用户ID 和 活动 修改 活动
    void modifyActivity(Long userId, SaleActivity saleActivity);
    
    // 根据 用户ID 和 活动ID 上线 活动
    void onlineActivity(Long userId, Long activityId);
    
    // 根据 用户ID 和 活动ID 下线 活动
    void offlineActivity(Long userId, Long activityId);
}
