package com.hanfei.flashsales.service;

import com.hanfei.flashsales.pojo.Activity;
import com.hanfei.flashsales.vo.ListVO;

import java.util.List;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
public interface ActivityService {

    List<Activity> getActiveActivities();

    List<ListVO> getActiveActivityVOs();

    Activity getActivityById(Long activityId);

    void updateActivity(Activity activity);

    // Optimistic lock version
    boolean lockStock(Long activityId);

    // Original version
    void lockStockNoLock(Long activityId);

    int revertStock(Long activityId);

    int deductStock(Long activityId);

    int addActivity(Activity activity);
}
