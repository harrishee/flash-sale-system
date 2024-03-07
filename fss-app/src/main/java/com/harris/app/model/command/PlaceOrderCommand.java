package com.harris.app.model.command;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PlaceOrderCommand {
    private Long id;
    private Long itemId;
    private Long activityId;
    private Long totalAmount;
    private Integer quantity;
    
    public boolean invalidParams() {
        return itemId == null || activityId == null || quantity == null || quantity <= 0;
    }
}
