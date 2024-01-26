package com.harris.app.model.converter;

import com.harris.app.model.PlaceOrderTask;
import com.harris.app.model.command.PurchaseCommand;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlaceOrderTaskConverter {
    public static PlaceOrderTask with(Long userId, PurchaseCommand purchaseCommand) {
        if (purchaseCommand == null) {
            return null;
        }
        PlaceOrderTask placeOrderTask = new PlaceOrderTask();
        BeanUtils.copyProperties(purchaseCommand, placeOrderTask);
        placeOrderTask.setUserId(userId);
        return placeOrderTask;
    }
}
