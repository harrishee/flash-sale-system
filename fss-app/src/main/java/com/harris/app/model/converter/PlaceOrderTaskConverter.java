package com.harris.app.model.converter;

import com.harris.app.model.PlaceOrderTask;
import com.harris.app.model.command.FlashPlaceOrderCommand;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlaceOrderTaskConverter {
    public static PlaceOrderTask with(Long userId, FlashPlaceOrderCommand flashPlaceOrderCommand) {
        if (flashPlaceOrderCommand == null) {
            return null;
        }
        PlaceOrderTask placeOrderTask = new PlaceOrderTask();
        BeanUtils.copyProperties(flashPlaceOrderCommand, placeOrderTask);
        placeOrderTask.setUserId(userId);
        return placeOrderTask;
    }
}
