package com.harris.app.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class FlashActivityDTO {
    private Long Id;
    private String activityName;
    private String activityDesc;
    private Integer status;
    private Date startTime;
    private Date endTime;
    private Long version;
}
