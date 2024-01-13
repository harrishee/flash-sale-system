package com.harris.app.model.dto;

import lombok.Data;

import java.util.Date;

@Data
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
}