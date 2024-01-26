package com.harris.infra.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class BucketDO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long itemId;
    private Integer totalStocksAmount;
    private Integer availableStocksAmount;
    private Integer status;
    private Integer serialNo;
    private Date modifiedTime;
    private Date createTime;
}
