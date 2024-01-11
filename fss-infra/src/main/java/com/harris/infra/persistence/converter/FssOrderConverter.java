package com.harris.infra.persistence.converter;

import com.harris.domain.model.entity.FssOrder;
import com.harris.infra.persistence.model.FssOrderDO;
import org.springframework.beans.BeanUtils;

/**
 * @author: harris
 * @summary: flash-sale-system
 */
public class FssOrderConverter {
    private FssOrderConverter() {
    }

    public static FssOrderDO toDataObjectForCreate(FssOrder fssOrder) {
        FssOrderDO fssOrderDO = new FssOrderDO();
        BeanUtils.copyProperties(fssOrder, fssOrderDO);
        return fssOrderDO;
    }

    public static FssOrder toDomainObject(FssOrderDO fssOrderDO) {
        FssOrder fssOrder = new FssOrder();
        BeanUtils.copyProperties(fssOrderDO, fssOrder);
        return fssOrder;
    }
}
