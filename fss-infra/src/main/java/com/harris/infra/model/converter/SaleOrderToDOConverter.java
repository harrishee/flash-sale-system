package com.harris.infra.model.converter;

import com.harris.domain.model.entity.SaleOrder;
import com.harris.infra.model.SaleOrderDO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SaleOrderToDOConverter {
    public static SaleOrderDO toDO(SaleOrder saleOrder) {
        SaleOrderDO saleOrderDO = new SaleOrderDO();
        BeanUtils.copyProperties(saleOrder, saleOrderDO);
        return saleOrderDO;
    }

    public static SaleOrder toDomainModel(SaleOrderDO saleOrderDO) {
        SaleOrder saleOrder = new SaleOrder();
        BeanUtils.copyProperties(saleOrderDO, saleOrder);
        return saleOrder;
    }
}
