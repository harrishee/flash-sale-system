package com.harris.app.model.dto;

import com.harris.domain.model.enums.FlashItemStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class FlashItemDTO {
    private Long id;
    private String itemTitle;
    private String itemSubTitle;
    private String itemDesc;
    private Integer initialStock;
    private Integer availableStock;
    private Long originalPrice;
    private Long flashPrice;
    private Integer status;
    private Long activityId;
    private Date startTime;
    private Date endTime;
    private Long version;

    public boolean isOnSale() {
        if (!FlashItemStatus.isOnline(status) || startTime == null || endTime == null) {
            return false;
        }
        Date now = new Date();
        return (startTime.equals(now) || startTime.before(now)) && endTime.after(now);
    }
}
