package com.harris.infra.mapper;

import com.harris.domain.model.PageQuery;
import com.harris.infra.model.SaleItemDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SaleItemMapper {
    SaleItemDO getItemById(@Param("itemId") Long itemId);

    List<SaleItemDO> getItemsByCondition(PageQuery pageQuery);

    Integer countItemsByCondition(PageQuery pageQuery);

    int insertItem(SaleItemDO saleItemDO);

    int updateItem(SaleItemDO saleItemDO);

    int reduceStockById(@Param("itemId") Long itemId, @Param("quantity") Integer quantity);

    int addStockById(@Param("itemId") Long itemId, @Param("quantity") Integer quantity);
}
