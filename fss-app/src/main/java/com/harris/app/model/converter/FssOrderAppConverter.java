package com.harris.app.model.converter;

import com.harris.app.model.PlaceOrderTask;
import com.harris.app.model.command.PurchaseCommand;
import com.harris.app.model.dto.SaleOrderDTO;
import com.harris.app.model.query.SaleOrdersQuery;
import com.harris.domain.model.PageQueryCondition;
import com.harris.domain.model.entity.SaleOrder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FssOrderAppConverter {
    public static SaleOrder toDomainModel(PurchaseCommand purchaseCommand) {
        if (purchaseCommand == null) {
            return null;
        }
        SaleOrder saleOrder = new SaleOrder();
        BeanUtils.copyProperties(purchaseCommand, saleOrder);
        return saleOrder;
    }

    public static SaleOrder toDomainModel(PlaceOrderTask placeOrderTask) {
        if (placeOrderTask == null) {
            return null;
        }
        SaleOrder saleOrder = new SaleOrder();
        BeanUtils.copyProperties(placeOrderTask, saleOrder);
        return saleOrder;
    }

    public static PageQueryCondition toQuery(SaleOrdersQuery saleOrdersQuery) {
        if (saleOrdersQuery == null) {
            return new PageQueryCondition();
        }
        PageQueryCondition pageQueryCondition = new PageQueryCondition();
        BeanUtils.copyProperties(saleOrdersQuery, pageQueryCondition);
        return pageQueryCondition;
    }

    public static SaleOrderDTO toDTO(SaleOrder saleOrder) {
        if (saleOrder == null) {
            return null;
        }
        SaleOrderDTO saleOrderDTO = new SaleOrderDTO();
        BeanUtils.copyProperties(saleOrder, saleOrderDTO);
        return saleOrderDTO;
    }
}
