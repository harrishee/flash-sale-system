package com.harris.domain.model;

import com.harris.domain.model.enums.BucketType;
import lombok.Data;

@Data
public class Bucket {
    private Long itemId;
    private Integer totalStocksAmount;
    private Integer availableStocksAmount;
    private Integer status;
    private Integer serialNo;

    public boolean isPrimary() {
        return BucketType.PRIMARY.getCode().equals(serialNo);
    }
}
