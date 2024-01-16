package com.harris.app.model.command;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BucketArrangementCommand {
    private Integer arrangeMode;
    private Integer bucketQuantity;
    private Integer totalStock;
}
