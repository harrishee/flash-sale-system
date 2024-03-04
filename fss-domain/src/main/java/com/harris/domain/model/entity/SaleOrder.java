package com.harris.domain.model.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SaleOrder implements Serializable {
    private Long id;
    private Long itemId;
    private Long activityId;
    private String itemTitle;
    private Long salePrice;
    private Integer quantity;
    private Long totalAmount;
    private Integer status;
    private Long userId;
    private Date createTime;
    
    public boolean invalidParams() {
        return itemId == null || activityId == null ||
                quantity == null || quantity <= 0 ||
                totalAmount == null || totalAmount < 0;
    }
}
