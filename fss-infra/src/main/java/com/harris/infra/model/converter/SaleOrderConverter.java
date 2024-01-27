package com.harris.infra.model.converter;

import com.harris.domain.model.entity.SaleOrder;
import com.harris.infra.model.SaleOrderDO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SaleOrderConverter {
    public static SaleOrderDO toDO(SaleOrder saleOrder) {
        if (saleOrder == null) {
            return null;
        }

        SaleOrderDO saleOrderDO = new SaleOrderDO();
        BeanUtils.copyProperties(saleOrder, saleOrderDO);
        return saleOrderDO;
    }

    public static SaleOrder toDomainModel(SaleOrderDO saleOrderDO) {
        if (saleOrderDO == null) {
            return null;
        }

        SaleOrder saleOrder = new SaleOrder();
        BeanUtils.copyProperties(saleOrderDO, saleOrder);
        return saleOrder;
    }
}
