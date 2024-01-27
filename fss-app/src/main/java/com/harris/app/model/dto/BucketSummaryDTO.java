package com.harris.app.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class BucketSummaryDTO {
    private Integer totalStock;
    private Integer availableStock;
    private List<BucketDTO> buckets;
}
