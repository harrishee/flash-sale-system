package com.hanfei.flashsales.mapper;

import com.hanfei.flashsales.pojo.Activity;
import com.hanfei.flashsales.vo.ListVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author: harris
 * @time: 2023
 * @summary: seckill
 */
@Mapper
public interface ActivityMapper {

    int insertActivity(Activity activity);

    Activity selectActivityById(Long activityId);

    List<Activity> selectActivitiesByStatus(int activityStatus);

    List<ListVO> selectActivityVOsByStatus(int i);

    int updateActivity(Activity activity);

    /**
     * 根据活动 id 锁定库存（可用库存减 1，锁定库存加 1）
     */
    boolean lockStockById(long activityId);

    /**
     * 根据 id 扣减库存（将锁定库存减 1）
     */
    int deductStockById(Long activityId);

    /**
     * 根据 id 回滚库存（将可用库存加 1，锁定库存减 1）
     */
    int revertStockById(Long activityId);

    int lockStockOptimisticLock(Long activityId);
}
