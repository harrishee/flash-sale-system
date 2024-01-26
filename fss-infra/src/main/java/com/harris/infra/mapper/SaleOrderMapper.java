package com.harris.infra.mapper;

import com.harris.domain.model.PageQueryCondition;
import com.harris.infra.model.SaleOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SaleOrderMapper {
    SaleOrderDO getOrderById(@Param("orderId") Long orderId);

    List<SaleOrderDO> getOrdersByCondition(PageQueryCondition pageQueryCondition);

    Integer countOrdersByCondition(PageQueryCondition pageQueryCondition);

    int insertOrder(SaleOrderDO saleOrderDO);

    int updateStatus(SaleOrderDO saleOrderDO);
}
