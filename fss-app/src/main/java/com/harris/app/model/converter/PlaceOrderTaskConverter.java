package com.harris.app.model.converter;

import com.harris.app.model.PlaceOrderTask;
import com.harris.app.model.command.PlaceOrderCommand;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlaceOrderTaskConverter {
    public static PlaceOrderTask toTask(Long userId, PlaceOrderCommand placeOrderCommand) {
        if (placeOrderCommand == null) {
            return null;
        }

        PlaceOrderTask placeOrderTask = new PlaceOrderTask();
        BeanUtils.copyProperties(placeOrderCommand, placeOrderTask);
        placeOrderTask.setUserId(userId);
        return placeOrderTask;
    }
}
