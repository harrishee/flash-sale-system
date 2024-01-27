package com.harris.domain.repository;

import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleActivity;

import java.util.List;
import java.util.Optional;

public interface SaleActivityRepository {
    /**
     * Find a sale activity by its ID.
     *
     * @param activityId The activity ID
     * @return Optional object of the sale activity
     */
    Optional<SaleActivity> findActivityById(Long activityId);

    /**
     * Find sale activities by the given condition.
     *
     * @param pageQuery The condition
     * @return List of sale activities
     */
    List<SaleActivity> findActivitiesByCondition(PageQuery pageQuery);

    /**
     * Count total sale activities by the given condition.
     *
     * @param pageQuery The condition
     * @return The count of sale activities
     */
    Integer countActivitiesByCondition(PageQuery pageQuery);

    /**
     * Saves a sale activity, either by inserting or updating it.
     *
     * @param saleActivity The sale activity
     * @return The count of effected rows
     */
    int saveActivity(SaleActivity saleActivity);
}
