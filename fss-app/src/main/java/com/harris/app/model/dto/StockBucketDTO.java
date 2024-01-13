package com.harris.app.model.dto;

import lombok.Data;

@Data
public class StockBucketDTO {
    private Integer totalStockAmount;
    private Integer availableStockAmount;
    private Integer status;
    private Integer serialNo;
}
