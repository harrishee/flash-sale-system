package com.harris.infra.mapper;

import com.harris.domain.model.PageQuery;
import com.harris.infra.model.SaleActivityDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SaleActivityMapper {
    SaleActivityDO getActivityById(@Param("activityId") Long activityId);

    List<SaleActivityDO> getActivitiesByCondition(PageQuery pageQuery);

    Integer countActivitiesByCondition(PageQuery pageQuery);

    int insertActivity(SaleActivityDO saleActivityDO);

    int updateActivity(SaleActivityDO saleActivityDO);
}
