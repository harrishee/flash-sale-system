package com.harris.infra.util;

import com.harris.domain.model.entity.SaleActivity;
import com.harris.domain.model.entity.SaleItem;
import com.harris.domain.model.entity.SaleOrder;
import com.harris.infra.model.SaleActivityDO;
import com.harris.infra.model.SaleItemDO;
import com.harris.infra.model.SaleOrderDO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InfraConverter {
    public static SaleActivityDO toSaleActivityDO(SaleActivity saleActivity) {
        if (saleActivity == null) {
            return null;
        }
        
        SaleActivityDO saleActivityDO = new SaleActivityDO();
        BeanUtils.copyProperties(saleActivity, saleActivityDO);
        return saleActivityDO;
    }
    
    public static SaleActivity toSaleActivityDomain(SaleActivityDO saleActivityDO) {
        if (saleActivityDO == null) {
            return null;
        }
        
        SaleActivity saleActivity = new SaleActivity();
        BeanUtils.copyProperties(saleActivityDO, saleActivity);
        return saleActivity;
    }
    
    public static SaleItemDO toSaleItemDO(SaleItem saleItem) {
        if (saleItem == null) {
            return null;
        }
        
        SaleItemDO saleItemDO = new SaleItemDO();
        BeanUtils.copyProperties(saleItem, saleItemDO);
        return saleItemDO;
    }
    
    public static SaleItem toSaleItemDomain(SaleItemDO saleItemDO) {
        if (saleItemDO == null) {
            return null;
        }
        
        SaleItem saleItem = new SaleItem();
        BeanUtils.copyProperties(saleItemDO, saleItem);
        return saleItem;
    }
    
    public static SaleOrderDO toSaleOrderDO(SaleOrder saleOrder) {
        if (saleOrder == null) {
            return null;
        }
        
        SaleOrderDO saleOrderDO = new SaleOrderDO();
        BeanUtils.copyProperties(saleOrder, saleOrderDO);
        return saleOrderDO;
    }
    
    public static SaleOrder toSaleOrderDomain(SaleOrderDO saleOrderDO) {
        if (saleOrderDO == null) {
            return null;
        }
        
        SaleOrder saleOrder = new SaleOrder();
        BeanUtils.copyProperties(saleOrderDO, saleOrder);
        return saleOrder;
    }
}
