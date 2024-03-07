package com.harris.infra.mapper;

import com.harris.domain.model.PageQuery;
import com.harris.infra.model.SaleOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SaleOrderMapper {
    SaleOrderDO getOrderById(@Param("orderId") Long orderId);
    
    List<SaleOrderDO> getOrdersByCondition(PageQuery pageQuery);
    
    Integer countOrdersByCondition(PageQuery pageQuery);
    
    int insertOrder(SaleOrderDO saleOrderDO);
    
    int updateStatus(SaleOrderDO saleOrderDO);
}
