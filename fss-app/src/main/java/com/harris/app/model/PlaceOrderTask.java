package com.harris.app.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PlaceOrderTask {
    String placeOrderTaskId;
    private Long userId;
    private Long id;
    private Long itemId;
    private Long activityId;
    private Integer quantity;
    private Long totalAmount;
}
