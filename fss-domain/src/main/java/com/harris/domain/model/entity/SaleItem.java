package com.harris.domain.model.entity;

import com.harris.domain.model.enums.SaleItemStatus;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;

@Data
public class SaleItem implements Serializable {
    private Long id;
    private String itemTitle;
    private String itemSubTitle;
    private String itemDesc;
    private Integer initialStock;
    private Integer availableStock;
    private Integer stockWarmUp;
    private Long originalPrice;
    private Integer status;
    private Long salePrice;
    private Long activityId;
    private Date startTime;
    private Date endTime;
    
    public boolean invalidParams() {
        return StringUtils.isEmpty(itemTitle) ||
                initialStock == null || initialStock <= 0 ||
                availableStock == null || availableStock <= 0 || availableStock > initialStock ||
                originalPrice == null || originalPrice < 0 ||
                salePrice == null || salePrice < 0 ||
                activityId == null ||
                startTime == null || endTime == null ||
                endTime.before(startTime) || endTime.before(new Date());
    }
    
    public boolean isOnline() {
        return SaleItemStatus.isOnline(status);
    }
    
    public boolean isInProgress() {
        Date now = new Date();
        return startTime.before(now) && endTime.after(now);
    }
}
