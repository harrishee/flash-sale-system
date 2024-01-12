package com.harris.domain.model.entity;

import lombok.Data;

@Data
public class Bucket {
    private Long itemId;
    private Integer totalStocksAmount;
    private Integer availableStocksAmount;
    private Integer status;
    private Integer serialNo;
}
