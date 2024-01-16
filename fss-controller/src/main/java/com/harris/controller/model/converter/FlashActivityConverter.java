package com.harris.controller.model.converter;

import com.harris.app.model.command.FlashActivityPublishCommand;
import com.harris.app.model.dto.FlashActivityDTO;
import com.harris.controller.model.request.FlashActivityPublishRequest;
import com.harris.controller.model.response.FlashActivityResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlashActivityConverter {
    public static FlashActivityPublishCommand toCommand(FlashActivityPublishRequest flashActivityPublishRequest) {
        if (flashActivityPublishRequest == null) {
            return null;
        }
        FlashActivityPublishCommand activityPublishCommand = new FlashActivityPublishCommand();
        BeanUtils.copyProperties(flashActivityPublishRequest, activityPublishCommand);
        return activityPublishCommand;
    }

    public static FlashActivityResponse toResponse(FlashActivityDTO flashActivityDTO) {
        if (flashActivityDTO == null) {
            return null;
        }
        FlashActivityResponse flashActivityResponse = new FlashActivityResponse();
        BeanUtils.copyProperties(flashActivityDTO, flashActivityResponse);
        return flashActivityResponse;
    }

    public static List<FlashActivityResponse> toResponses(Collection<FlashActivityDTO> flashActivityDTOS) {
        if (CollectionUtils.isEmpty(flashActivityDTOS)) {
            return new ArrayList<>();
        }
        return flashActivityDTOS.stream().map(FlashActivityConverter::toResponse).collect(Collectors.toList());
    }
}
