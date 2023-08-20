package com.hanfei.flashsales.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author: harris
 * @time: 2023
 * @summary: seckill
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commodity {

    private Long commodityId;

    private String commodityName;

    private BigDecimal commodityPrice;

    private String commodityDetail;

    private String commodityImg;
}
