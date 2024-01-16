package com.harris.controller.model.converter;

import com.harris.app.model.command.FlashItemPublishCommand;
import com.harris.app.model.dto.FlashItemDTO;
import com.harris.controller.model.request.FlashItemPublishRequest;
import com.harris.controller.model.response.FlashItemResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlashItemConverter {
    public static FlashItemPublishCommand toCommand(FlashItemPublishRequest flashItemPublishRequest) {
        if (flashItemPublishRequest == null) {
            return null;
        }
        FlashItemPublishCommand flashItemPublishCommand = new FlashItemPublishCommand();
        BeanUtils.copyProperties(flashItemPublishRequest, flashItemPublishCommand);
        return flashItemPublishCommand;
    }

    public static FlashItemResponse toResponse(FlashItemDTO flashItemDTO) {
        if (flashItemDTO == null) {
            return null;
        }
        FlashItemResponse flashItemResponse = new FlashItemResponse();
        BeanUtils.copyProperties(flashItemDTO, flashItemResponse);
        return flashItemResponse;
    }

    public static List<FlashItemResponse> toResponses(Collection<FlashItemDTO> flashItemDTOS) {
        if (CollectionUtils.isEmpty(flashItemDTOS)) {
            return new ArrayList<>();
        }
        return flashItemDTOS.stream().map(FlashItemConverter::toResponse).collect(Collectors.toList());
    }
}
