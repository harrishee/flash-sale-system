package com.harris.infra.persistence.converter;

import com.harris.domain.model.entity.FssItem;
import com.harris.infra.persistence.model.FssItemDO;
import org.springframework.beans.BeanUtils;

/**
 * @author: harris
 * @summary: flash-sale-system
 */
public class FssItemConverter {
    private FssItemConverter() {
    }

    public static FssItemDO toDataObject(FssItem fssItem) {
        FssItemDO fssItemDO = new FssItemDO();
        BeanUtils.copyProperties(fssItem, fssItemDO);
        return fssItemDO;
    }

    public static FssItem toDomainObject(FssItemDO fssItemDO) {
        FssItem fssItem = new FssItem();
        BeanUtils.copyProperties(fssItemDO, fssItem);
        return fssItem;
    }
}
