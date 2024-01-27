package com.harris.app.model.converter;

import com.harris.app.model.PlaceOrderTask;
import com.harris.app.model.command.PlaceOrderCommand;
import com.harris.app.model.dto.SaleOrderDTO;
import com.harris.app.model.query.SaleOrdersQuery;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleOrder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SaleOrderAppConverter {
    public static SaleOrder toDomainModel(PlaceOrderCommand placeOrderCommand) {
        if (placeOrderCommand == null) {
            return null;
        }

        SaleOrder saleOrder = new SaleOrder();
        BeanUtils.copyProperties(placeOrderCommand, saleOrder);
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

    public static PageQuery toQuery(SaleOrdersQuery saleOrdersQuery) {
        if (saleOrdersQuery == null) {
            return new PageQuery();
        }

        PageQuery pageQuery = new PageQuery();
        BeanUtils.copyProperties(saleOrdersQuery, pageQuery);
        return pageQuery;
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
