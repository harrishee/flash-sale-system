package com.harris.controller.model.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class SaleOrderResponse {
    private Long id;
    private Long itemId;
    private Long userId;
    private Long activityId;
    private Long totalAmount;
    private Integer quantity;
    private Integer status;
    private Date createTime;
}
