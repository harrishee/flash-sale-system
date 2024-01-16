package com.harris.controller.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class FlashItemPublishRequest {
    private String itemTitle;
    private String itemSubTitle;
    private String itemDesc;
    private Integer initialStock;
    private Integer availableStock;
    private Long originalPrice;
    private Long flashPrice;
    private Long activityId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
}
