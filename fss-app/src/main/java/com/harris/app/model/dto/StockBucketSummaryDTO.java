package com.harris.app.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class StockBucketSummaryDTO {
    private Integer totalStock;
    private Integer availableStock;
    private List<StockBucketDTO> buckets;
}
