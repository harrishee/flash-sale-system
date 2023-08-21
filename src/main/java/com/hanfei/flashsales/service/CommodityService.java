package com.hanfei.flashsales.service;

import com.hanfei.flashsales.pojo.Commodity;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
public interface CommodityService {

    Commodity getCommodityById(Long commodityId);

    int insertCommodity(Commodity commodity);
}
