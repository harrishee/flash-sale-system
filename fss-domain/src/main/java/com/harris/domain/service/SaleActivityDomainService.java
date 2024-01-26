package com.harris.domain.service;

import com.harris.domain.model.PageResult;
import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleActivity;

public interface SaleActivityDomainService {
    /**
     * Retrieve a SaleActivity by ID.
     *
     * @param activityId The activity ID
     * @return The SaleActivity
     */
    SaleActivity getActivity(Long activityId);

    /**
     * Retrieve a paginated list of SaleActivity by the given condition.
     *
     * @param pageQueryCondition The page query condition
     * @return The PageResult with SaleActivities and the total count
     */
    PageResult<SaleActivity> getActivities(PageQueryCondition pageQueryCondition);

    /**
     * Publish a SaleActivity and publish a publish activity event.
     *
     * @param userId       The user ID
     * @param saleActivity The SaleActivity
     */
    void publishActivity(Long userId, SaleActivity saleActivity);

    /**
     * Modify a SaleActivity and publish a modify activity event.
     *
     * @param userId       The user ID
     * @param saleActivity The SaleActivity
     */
    void modifyActivity(Long userId, SaleActivity saleActivity);

    /**
     * Online a SaleActivity and publish a online activity event.
     *
     * @param userId     The user ID
     * @param activityId The activity ID
     */
    void onlineActivity(Long userId, Long activityId);

    /**
     * Offline a SaleActivity and publish a offline activity event.
     *
     * @param userId     The user ID
     * @param activityId The activity ID
     */
    void offlineActivity(Long userId, Long activityId);
}
