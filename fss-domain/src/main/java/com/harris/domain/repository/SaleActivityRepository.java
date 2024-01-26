package com.harris.domain.repository;

import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleActivity;

import java.util.List;
import java.util.Optional;

public interface SaleActivityRepository {
    Optional<SaleActivity> findActivityById(Long activityId);

    List<SaleActivity> findActivitiesByCondition(PageQueryCondition pageQueryCondition);

    Integer countActivitiesByCondition(PageQueryCondition pageQueryCondition);

    int saveActivity(SaleActivity saleActivity);
}
