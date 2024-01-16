package com.harris.controller.model.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class FlashItemResponse {
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
