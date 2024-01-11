package com.harris.domain.model.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author: harris
 * @summary: flash-sale-system
 */
@Data
public class FssItem implements Serializable {
    private Long id;
    private Long activityId;
    private String itemTitle;
    private String itemSubTitle;
    private String itemDesc;
    private Integer initialStock;
    private Integer availableStock;
    private Long originalPrice;
    private Long flashPrice;
    private Date startTime;
    private Date endTime;
    private Integer status;
    private Integer stockWarmUp;
}
