package com.hanfei.flashsales.service;

import com.hanfei.flashsales.pojo.Activity;
import com.hanfei.flashsales.vo.ListVO;

import java.util.List;

/**
 * @author: harris
 * @time: 2023
 * @summary: seckill
 */
public interface ActivityService {

    List<Activity> getActiveActivities();

    List<ListVO> getActiveActivityVOs();

    Activity getActivityById(Long activityId);

    int updateActivity(Activity activity);

    boolean lockStock(Long activityId);

    int revertStock(Long activityId);

    int deductStock(Long activityId);

    int addActivity(Activity activity);

    int lockStockOptimisticLock(Long activityId);
}
