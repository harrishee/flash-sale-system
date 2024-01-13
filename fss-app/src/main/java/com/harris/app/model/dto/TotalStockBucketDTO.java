package com.harris.app.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class TotalStockBucketDTO {
    private Integer totalStocksAmount;
    private Integer availableStocksAmount;
    private List<StockBucketDTO> buckets;
}
