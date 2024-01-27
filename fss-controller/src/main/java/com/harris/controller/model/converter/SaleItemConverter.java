package com.harris.controller.model.converter;

import com.harris.app.model.command.PublishItemCommand;
import com.harris.app.model.dto.SaleItemDTO;
import com.harris.controller.model.request.PublishItemRequest;
import com.harris.controller.model.response.SaleItemResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SaleItemConverter {
    public static PublishItemCommand toCommand(PublishItemRequest publishItemRequest) {
        if (publishItemRequest == null) {
            return null;
        }

        PublishItemCommand publishItemCommand = new PublishItemCommand();
        BeanUtils.copyProperties(publishItemRequest, publishItemCommand);

        return publishItemCommand;
    }

    public static SaleItemResponse toResponse(SaleItemDTO saleItemDTO) {
        if (saleItemDTO == null) {
            return null;
        }

        SaleItemResponse saleItemResponse = new SaleItemResponse();
        BeanUtils.copyProperties(saleItemDTO, saleItemResponse);

        return saleItemResponse;
    }

    public static List<SaleItemResponse> toResponseList(Collection<SaleItemDTO> saleItemDTOS) {
        if (CollectionUtils.isEmpty(saleItemDTOS)) {
            return new ArrayList<>();
        }

        return saleItemDTOS.stream().map(SaleItemConverter::toResponse).collect(Collectors.toList());
    }
}
