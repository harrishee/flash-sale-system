package com.harris.app.model.converter;

import com.harris.app.model.command.FlashActivityPublishCommand;
import com.harris.app.model.dto.FlashActivityDTO;
import com.harris.app.model.query.FlashActivitiesQuery;
import com.harris.domain.model.PagesQueryCondition;
import com.harris.domain.model.entity.FlashActivity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlashActivityAppConverter {
    public static FlashActivity toDomainObject(FlashActivityPublishCommand flashActivityPublishCommand) {
        if (flashActivityPublishCommand == null) {
            return null;
        }
        FlashActivity flashActivity = new FlashActivity();
        BeanUtils.copyProperties(flashActivityPublishCommand, flashActivity);
        return flashActivity;
    }

    public static PagesQueryCondition toQuery(FlashActivitiesQuery flashActivitiesQuery) {
        if (flashActivitiesQuery == null) {
            return new PagesQueryCondition();
        }
        PagesQueryCondition pagesQueryCondition = new PagesQueryCondition();
        BeanUtils.copyProperties(flashActivitiesQuery, pagesQueryCondition);
        return pagesQueryCondition;
    }

    public static FlashActivityDTO toDTO(FlashActivity flashActivity) {
        if (flashActivity == null) {
            return null;
        }
        FlashActivityDTO flashActivityDTO = new FlashActivityDTO();
        BeanUtils.copyProperties(flashActivity, flashActivityDTO);
        return flashActivityDTO;
    }
}
