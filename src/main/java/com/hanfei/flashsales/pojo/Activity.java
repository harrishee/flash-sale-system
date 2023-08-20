package com.hanfei.flashsales.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author: harris
 * @time: 2023
 * @summary: seckill
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

    private Long activityId;

    private String activityName;

    private Long commodityId;

    private Long totalStock;

    private Long availableStock;

    private Long lockStock;

    private Integer activityStatus;

    private BigDecimal oldPrice;

    private BigDecimal salePrice;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
