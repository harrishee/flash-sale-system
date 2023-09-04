package com.hanfei.flashsales.mapper;

import com.hanfei.flashsales.pojo.Activity;
import com.hanfei.flashsales.vo.ListVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Mapper
public interface ActivityMapper {

    int insertActivity(Activity activity);

    Activity selectActivityById(Long activityId);

    List<Activity> selectActivitiesByStatus(int activityStatus);

    List<ListVO> selectActivityVOsByStatus(int i);

    int updateActivity(Activity activity);

    /**
     * Lock the stock of an activity by its ID (decrease available_stock by 1, increase lock_stock by 1)
     */
    boolean lockStockById(long activityId);

    /**
     * Deduct the stock of an activity by its ID (decrease lock_stock by 1)
     */
    int deductStockById(Long activityId);

    /**
     * Revert the stock of an activity by its ID (increase available_stock by 1, decrease lock_stock by 1)
     */
    int revertStockById(Long activityId);

    int lockStockNoOptimisticLock(Long activityId);
}
