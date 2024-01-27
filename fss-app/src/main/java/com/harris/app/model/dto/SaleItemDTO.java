package com.harris.app.model.dto;

import com.harris.domain.model.enums.SaleItemStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class SaleItemDTO {
    private Long id;
    private String itemTitle;
    private String itemSubTitle;
    private String itemDesc;
    private Integer initialStock;
    private Integer availableStock;
    private Long originalPrice;
    private Long salePrice;
    private Integer status;
    private Long activityId;
    private Date startTime;
    private Date endTime;
    private Long version;

    public boolean notOnSale() {
        if (!SaleItemStatus.isOnline(status) || startTime == null || endTime == null) {
            return true;
        }
        Date now = new Date();
        return (!startTime.equals(now) && !startTime.before(now)) || !endTime.after(now);
    }
}
