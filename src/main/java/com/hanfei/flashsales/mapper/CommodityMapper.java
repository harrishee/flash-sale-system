package com.hanfei.flashsales.mapper;

import com.hanfei.flashsales.pojo.Commodity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author: harris
 * @time: 2023
 * @summary: seckill
 */
@Mapper
public interface CommodityMapper {

    int insertCommodity(Commodity commodity);

    Commodity selectCommodityById(Long commodityId);
}
