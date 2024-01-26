package com.harris.infra.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SaleItemDO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String itemTitle;
    private String itemSubTitle;
    private String itemDesc;
    private Integer initialStock;
    private Integer availableStock;
    private Integer stockWarmUp;
    private Long originalPrice;
    private Long salePrice;
    private String rules;
    private Integer status;
    private Long activityId;
    private Date startTime;
    private Date endTime;
    private Date modifiedTime;
    private Date createTime;
}
