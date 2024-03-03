package com.harris.app.util;

import com.harris.app.model.PlaceOrderTask;
import com.harris.app.model.command.PlaceOrderCommand;
import com.harris.app.model.command.PublishActivityCommand;
import com.harris.app.model.command.PublishItemCommand;
import com.harris.app.model.dto.SaleActivityDTO;
import com.harris.app.model.dto.SaleItemDTO;
import com.harris.app.model.dto.SaleOrderDTO;
import com.harris.domain.model.PageQuery;
import com.harris.domain.model.entity.SaleActivity;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.model.entity.SaleOrder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppConverter {
    public static SaleActivity toDomainModel(PublishActivityCommand publishActivityCommand) {
        if (publishActivityCommand == null) return null;
        SaleActivity saleActivity = new SaleActivity();
        BeanUtils.copyProperties(publishActivityCommand, saleActivity);
        return saleActivity;
    }
    
    public static SaleActivityDTO toDTO(SaleActivity saleActivity) {
        if (saleActivity == null) return null;
        SaleActivityDTO saleActivityDTO = new SaleActivityDTO();
        BeanUtils.copyProperties(saleActivity, saleActivityDTO);
        return saleActivityDTO;
    }
    
    public static PageQuery toPageQuery(Object query) {
        if (query == null) return new PageQuery();
        PageQuery pageQuery = new PageQuery();
        BeanUtils.copyProperties(query, pageQuery);
        return pageQuery;
    }
    
    public static SaleItem toDomainModel(PublishItemCommand publishItemCommand) {
        if (publishItemCommand == null) return null;
        SaleItem saleItem = new SaleItem();
        BeanUtils.copyProperties(publishItemCommand, saleItem);
        return saleItem;
    }
    
    public static SaleItemDTO toDTO(SaleItem saleItem) {
        if (saleItem == null) return null;
        SaleItemDTO saleItemDTO = new SaleItemDTO();
        BeanUtils.copyProperties(saleItem, saleItemDTO);
        return saleItemDTO;
    }
    
    public static SaleOrder toDomainModel(PlaceOrderCommand placeOrderCommand) {
        if (placeOrderCommand == null) return null;
        SaleOrder saleOrder = new SaleOrder();
        BeanUtils.copyProperties(placeOrderCommand, saleOrder);
        return saleOrder;
    }
    
    public static SaleOrder toDomainModel(PlaceOrderTask placeOrderTask) {
        if (placeOrderTask == null) return null;
        SaleOrder saleOrder = new SaleOrder();
        BeanUtils.copyProperties(placeOrderTask, saleOrder);
        return saleOrder;
    }
    
    public static SaleOrderDTO toDTO(SaleOrder saleOrder) {
        if (saleOrder == null) return null;
        SaleOrderDTO saleOrderDTO = new SaleOrderDTO();
        BeanUtils.copyProperties(saleOrder, saleOrderDTO);
        return saleOrderDTO;
    }
    
    public static PlaceOrderTask toTask(Long userId, PlaceOrderCommand placeOrderCommand) {
        if (placeOrderCommand == null) return null;
        PlaceOrderTask placeOrderTask = new PlaceOrderTask();
        BeanUtils.copyProperties(placeOrderCommand, placeOrderTask);
        placeOrderTask.setUserId(userId);
        return placeOrderTask;
    }
}
