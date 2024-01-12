package com.harris.domain.repository;

import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashActivity;

import java.util.List;
import java.util.Optional;

public interface FssActivityRepository {
    int saveActivity(FlashActivity flashActivity);

    Optional<FlashActivity> findActivityById(Long activityId);

    List<FlashActivity> findActivitiesByCondition(PagesQueryCondition pagesQueryCondition);

    Integer countActivitiesByCondition(PagesQueryCondition pagesQueryCondition);
}
