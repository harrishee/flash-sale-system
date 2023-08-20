package com.hanfei.flashsales.service;

import com.hanfei.flashsales.pojo.Commodity;

/**
 * @author: harris
 * @time: 2023
 * @summary: seckill
 */
public interface CommodityService {

    Commodity getCommodityById(Long commodityId);

    int insertCommodity(Commodity commodity);
}
