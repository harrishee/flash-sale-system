package com.hanfei.flashsales.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Long orderId;

    private String orderNo;

    private Integer orderStatus;

    private BigDecimal orderAmount;

    private Long userId;

    private Long activityId;

    private Long commodityId;

    private LocalDateTime createTime;

    private LocalDateTime payTime;
}
