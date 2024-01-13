package com.harris.app.model.query;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FlashItemsQuery {
    private String keyword;
    private Integer pageSize;
    private Integer pageNumber;
    private Integer status;
    private Long version;
    private Long activityId;
}
