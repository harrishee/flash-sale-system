package com.hanfei.flashsales.service.impl;

import com.hanfei.flashsales.mapper.ActivityMapper;
import com.hanfei.flashsales.pojo.Activity;
import com.hanfei.flashsales.service.ActivityService;
import com.hanfei.flashsales.vo.ListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: harris
 * @time: 2023
 * @summary: seckill
 */
@Slf4j
@Service
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private ActivityMapper activityMapper;

    public List<Activity> getActiveActivities() {
        return activityMapper.selectActivitiesByStatus(1);
    }

    @Override
    public List<ListVO> getActiveActivityVOs() {
        return activityMapper.selectActivityVOsByStatus(1);
    }

    @Override
    public Activity getActivityById(Long activityId) {
        return activityMapper.selectActivityById(activityId);
    }

    @Override
    public int updateActivity(Activity activity) {
        return activityMapper.updateActivity(activity);
    }

    @Override
    public boolean lockStock(Long activityId) {
        return activityMapper.lockStockById(activityId);
    }

    @Override
    public int revertStock(Long activityId) {
        return activityMapper.revertStockById(activityId);
    }

    @Override
    public int deductStock(Long activityId) {
        return activityMapper.deductStockById(activityId);
    }

    @Override
    public int addActivity(Activity activity) {
        return activityMapper.insertActivity(activity);
    }

    @Override
    public int lockStockOptimisticLock(Long activityId) {
        return activityMapper.lockStockOptimisticLock(activityId);
    }
}
