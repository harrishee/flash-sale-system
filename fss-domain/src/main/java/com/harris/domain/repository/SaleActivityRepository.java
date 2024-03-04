package com.harris.domain.repository;

import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleActivity;

import java.util.List;
import java.util.Optional;

public interface SaleActivityRepository {
    Optional<SaleActivity> findActivityById(Long activityId);
    
    List<SaleActivity> findAllActivityByCondition(PageQuery pageQuery);
    
    Integer countAllActivityByCondition(PageQuery pageQuery);
    
    int saveActivity(SaleActivity saleActivity);
}
