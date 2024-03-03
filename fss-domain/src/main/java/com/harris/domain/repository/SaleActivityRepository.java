package com.harris.domain.repository;

import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleActivity;

import java.util.List;
import java.util.Optional;

public interface SaleActivityRepository {
    // 根据 活动ID 查询 活动
    Optional<SaleActivity> findActivityById(Long activityId);
    
    // 根据 页面查询条件 查询 活动列表
    List<SaleActivity> findAllActivityByCondition(PageQuery pageQuery);
    
    // 根据 页面查询条件 查询 活动数量
    Integer countAllActivityByCondition(PageQuery pageQuery);

    // 保存 活动
    int saveActivity(SaleActivity saleActivity);
}
