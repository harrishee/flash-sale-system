package com.harris.app.model.query;

import com.harris.domain.model.enums.SaleItemStatus;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

@Data
@Accessors(chain = true)
public class SaleItemsQuery {
    private String keyword;
    private Integer pageSize;
    private Integer pageNumber;
    private Integer status;
    private Long version;
    private Long activityId;
    
    public boolean isOnlineFirstPageAndNoKeywordQuery() {
        return StringUtils.isEmpty(keyword) && pageNumber != null && pageNumber == 1 && SaleItemStatus.isOnline(status);
    }
}
