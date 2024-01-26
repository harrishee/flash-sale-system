package com.harris.infra.mapper;

import com.harris.domain.model.PageQueryCondition;
import com.harris.infra.model.SaleActivityDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SaleActivityMapper {
    SaleActivityDO getActivityById(@Param("activityId") Long activityId);

    List<SaleActivityDO> getActivitiesByCondition(PageQueryCondition pageQueryCondition);

    Integer countActivitiesByCondition(PageQueryCondition pageQueryCondition);

    int insertActivity(SaleActivityDO saleActivityDO);

    int updateActivity(SaleActivityDO saleActivityDO);
}
