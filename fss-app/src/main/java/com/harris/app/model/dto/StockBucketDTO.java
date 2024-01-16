package com.harris.app.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StockBucketDTO {
    private Integer totalStock;
    private Integer availableStock;
    private Integer status;
    private Integer serialNo;
}
