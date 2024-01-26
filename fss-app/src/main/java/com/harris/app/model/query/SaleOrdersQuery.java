package com.harris.app.model.query;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SaleOrdersQuery {
    private String keyword;
    private Integer pageSize;
    private Integer pageNumber;
    private Integer status;
}
