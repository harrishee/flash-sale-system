package com.hanfei.flashsales.service.impl;

import com.hanfei.flashsales.mapper.CommodityMapper;
import com.hanfei.flashsales.pojo.Commodity;
import com.hanfei.flashsales.service.CommodityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Slf4j
@Service
public class CommodityServiceImpl implements CommodityService {

    @Autowired
    private CommodityMapper commodityMapper;

    @Override
    public Commodity getCommodityById(Long commodityId) {
        return commodityMapper.selectCommodityById(commodityId);
    }

    @Override
    public int insertCommodity(Commodity commodity) {
        return commodityMapper.insertCommodity(commodity);
    }
}
