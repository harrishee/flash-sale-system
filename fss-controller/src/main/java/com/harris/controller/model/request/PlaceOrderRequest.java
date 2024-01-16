package com.harris.controller.model.request;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PlaceOrderRequest {
    private Long id;
    private Long itemId;
    private Long activityId;
    private Integer quantity;
    private Long totalAmount;
}
