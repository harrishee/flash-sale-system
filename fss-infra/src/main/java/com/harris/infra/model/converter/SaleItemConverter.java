package com.harris.infra.model.converter;

import com.harris.domain.model.entity.SaleItem;
import com.harris.infra.model.SaleItemDO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SaleItemConverter {
    public static SaleItemDO toDO(SaleItem saleItem) {
        if (saleItem == null) {
            return null;
        }

        SaleItemDO saleItemDO = new SaleItemDO();
        BeanUtils.copyProperties(saleItem, saleItemDO);
        return saleItemDO;
    }

    public static SaleItem toDomainModel(SaleItemDO saleItemDO) {
        if (saleItemDO == null) {
            return null;
        }

        SaleItem saleItem = new SaleItem();
        BeanUtils.copyProperties(saleItemDO, saleItem);
        return saleItem;
    }
}
