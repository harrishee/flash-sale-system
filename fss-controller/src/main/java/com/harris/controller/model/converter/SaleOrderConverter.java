package com.harris.controller.model.converter;

import com.harris.app.model.command.PlaceOrderCommand;
import com.harris.app.model.dto.SaleOrderDTO;
import com.harris.controller.model.request.PlaceOrderRequest;
import com.harris.controller.model.response.SaleOrderResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SaleOrderConverter {
    public static PlaceOrderCommand toCommand(PlaceOrderRequest placeOrderRequest) {
        if (placeOrderRequest == null) return null;
        PlaceOrderCommand placeOrderCommand = new PlaceOrderCommand();
        BeanUtils.copyProperties(placeOrderRequest, placeOrderCommand);
        return placeOrderCommand;
    }
    
    public static SaleOrderResponse toResponse(SaleOrderDTO saleOrderDTO) {
        if (saleOrderDTO == null) return null;
        SaleOrderResponse saleOrderResponse = new SaleOrderResponse();
        BeanUtils.copyProperties(saleOrderDTO, saleOrderResponse);
        return saleOrderResponse;
    }
    
    public static List<SaleOrderResponse> toResponseList(Collection<SaleOrderDTO> saleOrderDTOS) {
        if (CollectionUtils.isEmpty(saleOrderDTOS)) return new ArrayList<>();
        return saleOrderDTOS.stream().map(SaleOrderConverter::toResponse).collect(Collectors.toList());
    }
}
