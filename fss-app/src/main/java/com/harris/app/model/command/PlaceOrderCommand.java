package com.harris.app.model.command;

import lombok.Data;

@Data
public class PlaceOrderCommand {
    private Long id;
    private Long itemId;
    private Long activityId;
    private Long totalAmount;
    private Integer quantity;
}
