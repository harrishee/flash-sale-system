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

    // 乐观锁版本
    boolean lockStock(Long activityId);

    // 原始版本
    void lockStockNoLock(Long activityId);

    int revertStock(Long activityId);

    int deductStock(Long activityId);

    int addActivity(Activity activity);
}
