package com.hanfei.flashsales.config;

import com.hanfei.flashsales.controller.SaleController;
import com.hanfei.flashsales.mapper.ActivityMapper;
import com.hanfei.flashsales.pojo.Activity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Slf4j
@Component
public class RedisPreheat implements ApplicationRunner {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private SaleController saleController;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        List<Activity> activityList = activityMapper.selectActivitiesByStatus(1);
        if (CollectionUtils.isEmpty(activityList)) {
            log.info("***Redis*** 预热，没有进行中的活动");
            return;
        }
        activityList.forEach(activity -> {
            redisTemplate.opsForValue().set("activity:" + activity.getActivityId(), activity.getAvailableStock());
            saleController.getEmptyStockMap().put(activity.getActivityId(), false);
        });
        log.info("***Redis*** 预热成功，有 {} 条进行中的活动", activityList.size());
        log.info("***Redis*** 内存标记初始化成功，EmptyStockMap: {}", saleController.getEmptyStockMap());
    }
}
