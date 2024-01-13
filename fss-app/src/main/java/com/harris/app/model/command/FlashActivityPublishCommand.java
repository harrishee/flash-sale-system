package com.harris.app.model.command;

import lombok.Data;

import java.util.Date;

@Data
public class FlashActivityPublishCommand {
    private String activityName;
    private String activityDesc;
    private Integer status;
    private Date startTime;
    private Date endTime;
}
