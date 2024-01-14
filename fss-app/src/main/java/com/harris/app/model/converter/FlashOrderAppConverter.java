package com.harris.app.model.converter;

import com.harris.app.model.PlaceOrderTask;
import com.harris.app.model.command.PlaceOrderCommand;
import com.harris.app.model.dto.FlashOrderDTO;
import com.harris.app.model.query.FlashOrdersQuery;
import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashOrder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlashOrderAppConverter {
    public static FlashOrder toDomainObject(PlaceOrderCommand placeOrderCommand) {
        if (placeOrderCommand == null) {
            return null;
        }
        FlashOrder flashOrder = new FlashOrder();
        BeanUtils.copyProperties(placeOrderCommand, flashOrder);
        return flashOrder;
    }

    public static FlashOrder toDomainObject(PlaceOrderTask PlaceOrderTask) {
        if (PlaceOrderTask == null) {
            return null;
        }
        FlashOrder flashOrder = new FlashOrder();
        BeanUtils.copyProperties(PlaceOrderTask, flashOrder);
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
