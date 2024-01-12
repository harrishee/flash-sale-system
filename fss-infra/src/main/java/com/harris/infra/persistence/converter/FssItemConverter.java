package com.harris.infra.persistence.converter;

import com.harris.domain.model.entity.FlashItem;
import com.harris.infra.persistence.model.FlashItemDO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FssItemConverter {

    public static FlashItemDO toDataObject(FlashItem flashItem) {
        FlashItemDO flashItemDO = new FlashItemDO();
        BeanUtils.copyProperties(flashItem, flashItemDO);
        return flashItemDO;
    }

    public static FlashItem toDomainObject(FlashItemDO flashItemDO) {
        FlashItem flashItem = new FlashItem();
        BeanUtils.copyProperties(flashItemDO, flashItem);
        return flashItem;
    }
}
