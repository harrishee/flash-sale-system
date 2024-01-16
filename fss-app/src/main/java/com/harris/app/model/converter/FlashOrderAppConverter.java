package com.harris.app.model.converter;

import com.harris.app.model.PlaceOrderTask;
import com.harris.app.model.command.FlashPlaceOrderCommand;
import com.harris.app.model.dto.FlashOrderDTO;
import com.harris.app.model.query.FlashOrdersQuery;
import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashOrder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlashOrderAppConverter {
    public static FlashOrder toDomainObj(FlashPlaceOrderCommand flashPlaceOrderCommand) {
        if (flashPlaceOrderCommand == null) {
            return null;
        }
        FlashOrder flashOrder = new FlashOrder();
        BeanUtils.copyProperties(flashPlaceOrderCommand, flashOrder);
        return flashOrder;
    }

    public static FlashOrder toDomainObj(PlaceOrderTask placeOrderTask) {
        if (placeOrderTask == null) {
            return null;
        }
        FlashOrder flashOrder = new FlashOrder();
        BeanUtils.copyProperties(placeOrderTask, flashOrder);
        return flashOrder;
    }

    public static PagesQueryCondition toQuery(FlashOrdersQuery flashOrdersQuery) {
        if (flashOrdersQuery == null) {
            return new PagesQueryCondition();
        }
        PagesQueryCondition pagesQueryCondition = new PagesQueryCondition();
        BeanUtils.copyProperties(flashOrdersQuery, pagesQueryCondition);
        return pagesQueryCondition;
    }

    public static FlashOrderDTO toDTO(FlashOrder flashOrder) {
        if (flashOrder == null) {
            return null;
        }
        FlashOrderDTO flashOrderDTO = new FlashOrderDTO();
        BeanUtils.copyProperties(flashOrder, flashOrderDTO);
        return flashOrderDTO;
    }
}
