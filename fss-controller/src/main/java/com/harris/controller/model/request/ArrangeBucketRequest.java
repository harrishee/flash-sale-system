package com.harris.controller.model.request;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ArrangeBucketRequest {
    private Integer arrangeMode;
    private Integer bucketQuantity;
    private Integer totalStock;
}
