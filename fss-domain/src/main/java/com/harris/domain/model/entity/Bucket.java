package com.harris.domain.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: harris
 * @summary: flash-sale-system
 */
@Data
@Accessors(chain = true)
public class Bucket {
    private Integer serialNo;
    private Integer totalStocksAmount;
    private Integer availableStocksAmount;
    private Integer status;
    private Long itemId;
}
