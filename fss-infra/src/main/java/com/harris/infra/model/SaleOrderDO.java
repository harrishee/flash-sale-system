package com.harris.infra.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SaleOrderDO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long itemId;
    private Long activityId;
    private String itemTitle;
    private Long salePrice;
    private Integer quantity;
    private Long totalAmount;
    private Integer status;
    private Long userId;
    private Date modifiedTime;
    private Date createTime;
}
