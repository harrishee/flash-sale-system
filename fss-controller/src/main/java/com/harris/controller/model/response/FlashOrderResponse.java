package com.harris.controller.model.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class FlashOrderResponse {
    private Long id;
    private Long itemId;
    private Long activityId;
    private Integer quantity;
    private Long totalAmount;
    private Integer status;
    private Long userId;
    private Date createTime;
}
