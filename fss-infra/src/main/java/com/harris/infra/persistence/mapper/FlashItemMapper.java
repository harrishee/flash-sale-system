package com.harris.infra.persistence.mapper;

import com.harris.domain.model.PagesQueryCondition;
import com.harris.infra.persistence.model.FlashItemDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FlashItemMapper {
    int insertItem(FlashItemDO flashItemDO);

    int updateItem(FlashItemDO flashItemDO);

    FlashItemDO getItemById(@Param("itemId") Long itemId);

    List<FlashItemDO> getItemsByCondition(PagesQueryCondition pagesQueryCondition);

    Integer countItemsByCondition(PagesQueryCondition pagesQueryCondition);

    int decreaseStockById(@Param("itemId") Long itemId, @Param("quantity") Integer quantity);

    int increaseStockById(@Param("itemId") Long itemId, @Param("quantity") Integer quantity);
}
