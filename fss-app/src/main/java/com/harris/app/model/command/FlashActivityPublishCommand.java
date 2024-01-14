package com.harris.app.model.command;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

@Data
public class FlashActivityPublishCommand {
    private String activityName;
    private String activityDesc;
    private Integer status;
    private Date startTime;
    private Date endTime;

    public boolean invalidParams() {
        return !StringUtils.isNotEmpty(activityName) || startTime == null || endTime == null || !startTime.before(endTime);
    }
}
