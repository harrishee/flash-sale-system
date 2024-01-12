package com.harris.domain.model.entity;

import lombok.Data;

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
}
