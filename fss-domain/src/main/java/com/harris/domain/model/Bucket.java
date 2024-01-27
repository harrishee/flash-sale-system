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
    private Integer totalStock;
    private Integer availableStock;
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

    public void addAvailableStock(int availableStock) {
        if (this.availableStock == null) {
            return;
        }
        this.availableStock += availableStock;
    }

    public void increaseTotalStock(Integer increaseStockAmount) {
        if (increaseStockAmount == null) {
            return;
        }
        this.totalStock += increaseStockAmount;
    }
}
