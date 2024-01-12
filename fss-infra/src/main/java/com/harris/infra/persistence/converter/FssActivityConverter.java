package com.harris.infra.persistence.converter;

import com.harris.domain.model.entity.FlashActivity;
import com.harris.infra.persistence.model.FlashActivityDO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FssActivityConverter {

    public static FlashActivityDO toDataObject(FlashActivity flashActivity) {
        FlashActivityDO flashActivityDO = new FlashActivityDO();
        BeanUtils.copyProperties(flashActivity, flashActivityDO);
        return flashActivityDO;
    }

    public static FlashActivity toDomainObject(FlashActivityDO flashActivityDO) {
        FlashActivity flashActivity = new FlashActivity();
        BeanUtils.copyProperties(flashActivityDO, flashActivity);
        return flashActivity;
    }
}
