package com.harris.controller.model.converter;

import com.harris.app.model.command.FlashPlaceOrderCommand;
import com.harris.app.model.dto.FlashOrderDTO;
import com.harris.controller.model.request.PlaceOrderRequest;
import com.harris.controller.model.response.FlashOrderResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlashOrderConverter {
    public static FlashPlaceOrderCommand toCommand(PlaceOrderRequest placeOrderRequest) {
        FlashPlaceOrderCommand flashPlaceOrderCommand = new FlashPlaceOrderCommand();
        BeanUtils.copyProperties(placeOrderRequest, flashPlaceOrderCommand);
        return flashPlaceOrderCommand;
    }

    public static FlashOrderResponse toResponse(FlashOrderDTO flashOrderDTO) {
        if (flashOrderDTO == null) {
            return null;
        }
        FlashOrderResponse flashOrderResponse = new FlashOrderResponse();
        BeanUtils.copyProperties(flashOrderDTO, flashOrderResponse);
        return flashOrderResponse;
    }

    public static List<FlashOrderResponse> toResponses(Collection<FlashOrderDTO> flashOrderDTOS) {
        if (CollectionUtils.isEmpty(flashOrderDTOS)) {
            return new ArrayList<>();
        }
        return flashOrderDTOS.stream().map(FlashOrderConverter::toResponse).collect(Collectors.toList());
    }
}
