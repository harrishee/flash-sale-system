package com.harris.domain.model;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import static com.harris.domain.model.enums.BucketType.PRIMARY;

@Data
@Getter
@Accessors(chain = true)
public class Bucket {
    private Long itemId;
    private Integer totalStocksAmount;
    private Integer availableStocksAmount;
    private Integer status;
    private Integer serialNo;

    public boolean isPrimary() {
        return PRIMARY.getCode().equals(serialNo);
    }

    public boolean isSubBucket() {
        return !PRIMARY.getCode().equals(serialNo);
    }

    public Bucket initPrimary() {
        this.serialNo = PRIMARY.getCode();
        return this;
    }

    public void addAvailableStock(int availableStockAmount) {
        if (this.availableStocksAmount == null) {
            return;
        }
        this.availableStocksAmount += availableStockAmount;
    }

    public void increaseTotalStockAmount(Integer increaseStockAmount) {
        if (increaseStockAmount == null) {
            return;
        }
        this.totalStocksAmount += increaseStockAmount;
    }
}
