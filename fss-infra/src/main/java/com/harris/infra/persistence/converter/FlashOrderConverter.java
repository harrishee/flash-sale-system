package com.harris.infra.persistence.converter;

import com.harris.domain.model.entity.FlashOrder;
import com.harris.infra.persistence.model.FlashOrderDO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlashOrderConverter {

    public static FlashOrderDO toDataObject(FlashOrder flashOrder) {
        FlashOrderDO flashOrderDO = new FlashOrderDO();
        BeanUtils.copyProperties(flashOrder, flashOrderDO);
        return flashOrderDO;
    }

    public static FlashOrder toDomainObject(FlashOrderDO flashOrderDO) {
        FlashOrder flashOrder = new FlashOrder();
        BeanUtils.copyProperties(flashOrderDO, flashOrder);
        return flashOrder;
    }
}
