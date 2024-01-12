package com.harris.infra.persistence.mapper;

import com.harris.domain.model.PagesQueryCondition;
import com.harris.infra.persistence.model.FlashActivityDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FlashActivityMapper {
    int insertActivity(FlashActivityDO flashActivityDO);

    int updateActivity(FlashActivityDO flashActivityDO);

    FlashActivityDO getActivityById(@Param("activityId") Long activityId);

    List<FlashActivityDO> getActivitiesByCondition(PagesQueryCondition pagesQueryCondition);

    Integer countActivitiesByCondition(PagesQueryCondition pagesQueryCondition);
}
