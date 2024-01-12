package com.harris.infra.persistence.mapper;

import com.harris.domain.model.PagesQueryCondition;
import com.harris.infra.persistence.model.FlashOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FssOrderMapper {
    int insertOrder(FlashOrderDO flashOrderDO);

    int updateStatus(FlashOrderDO flashOrderDO);

    FlashOrderDO getOrderById(@Param("orderId") Long orderId);

    List<FlashOrderDO> getOrdersByCondition(PagesQueryCondition pagesQueryCondition);

    Integer countOrdersByCondition();
}
