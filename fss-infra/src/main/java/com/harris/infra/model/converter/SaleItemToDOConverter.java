package com.harris.infra.model.converter;

import com.harris.domain.model.entity.SaleItem;
import com.harris.infra.model.SaleItemDO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SaleItemToDOConverter {
    public static SaleItemDO toDO(SaleItem saleItem) {
        SaleItemDO saleItemDO = new SaleItemDO();
        BeanUtils.copyProperties(saleItem, saleItemDO);
        return saleItemDO;
    }

    public static SaleItem toDomainModel(SaleItemDO saleItemDO) {
        SaleItem saleItem = new SaleItem();
        BeanUtils.copyProperties(saleItemDO, saleItem);
        return saleItem;
    }
}
