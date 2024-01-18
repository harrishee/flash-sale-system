package com.harris.controller.model.converter;

import com.harris.app.model.command.PublishActivityCommand;
import com.harris.app.model.dto.SaleActivityDTO;
import com.harris.controller.model.request.PublishActivityRequest;
import com.harris.controller.model.response.SaleActivityResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SaleActivityConverter {
    public static PublishActivityCommand toCommand(PublishActivityRequest publishActivityRequest) {
        if (publishActivityRequest == null) {
            return null;
        }
        PublishActivityCommand activityPublishCommand = new PublishActivityCommand();
        BeanUtils.copyProperties(publishActivityRequest, activityPublishCommand);
        return activityPublishCommand;
    }

    public static SaleActivityResponse toResponse(SaleActivityDTO saleActivityDTO) {
        if (saleActivityDTO == null) {
            return null;
        }
        SaleActivityResponse saleActivityResponse = new SaleActivityResponse();
        BeanUtils.copyProperties(saleActivityDTO, saleActivityResponse);
        return saleActivityResponse;
    }

    public static List<SaleActivityResponse> toResponseList(Collection<SaleActivityDTO> saleActivityDTOS) {
        if (CollectionUtils.isEmpty(saleActivityDTOS)) {
            return new ArrayList<>();
        }
        return saleActivityDTOS.stream().map(SaleActivityConverter::toResponse).collect(Collectors.toList());
    }
}
