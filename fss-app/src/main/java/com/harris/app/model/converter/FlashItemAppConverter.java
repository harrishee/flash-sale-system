package com.harris.app.model.converter;

import com.harris.app.model.command.FlashItemPublishCommand;
import com.harris.app.model.dto.FlashItemDTO;
import com.harris.app.model.query.FlashItemsQuery;
import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashItem;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlashItemAppConverter {
    public static FlashItem toDomainObject(FlashItemPublishCommand flashItemPublishCommand) {
        FlashItem flashItem = new FlashItem();
        BeanUtils.copyProperties(flashItemPublishCommand, flashItem);
        return flashItem;
    }

    public static PagesQueryCondition toQuery(FlashItemsQuery flashItemsQuery) {
        PagesQueryCondition pagesQueryCondition = new PagesQueryCondition();
        BeanUtils.copyProperties(flashItemsQuery, pagesQueryCondition);
        return pagesQueryCondition;
    }

    public static FlashItemDTO toDTO(FlashItem flashItem) {
        FlashItemDTO flashItemDto = new FlashItemDTO();
        BeanUtils.copyProperties(flashItem, flashItemDto);
        return flashItemDto;
    }
}
