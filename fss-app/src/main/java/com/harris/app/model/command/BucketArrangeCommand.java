package com.harris.app.model.command;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BucketArrangeCommand {
    private Integer arrangeMode;
    private Integer bucketQuantity;
    private Integer totalStocksAmount;
}
