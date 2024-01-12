package com.harris.domain.model.entity;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;

@Data
public class FlashItem implements Serializable {
    private Long id;
    private String itemTitle;
    private String itemSubTitle;
    private String itemDesc;
    private Integer initialStock;
    private Integer availableStock;
    private Integer stockWarmUp;
    private Long originalPrice;
    private Integer status;
    private Long flashPrice;
    private Long activityId;
    private Date startTime;
    private Date endTime;

    public boolean invalidParams() {
        return StringUtils.isEmpty(itemTitle) ||
                initialStock == null || initialStock <= 0 ||
                availableStock == null || availableStock <= 0 || availableStock > initialStock ||
                originalPrice == null || originalPrice < 0 ||
                flashPrice == null || flashPrice < 0 ||
                activityId == null ||
                startTime == null || endTime == null ||
                endTime.before(startTime) || endTime.before(new Date());
    }
}
