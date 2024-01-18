package com.harris.app.model.command;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

@Data
@Accessors(chain = true)
public class SaleActivityPublishCommand {
    private String activityName;
    private String activityDesc;
    private Integer status;
    private Date startTime;
    private Date endTime;

    public boolean invalidParams() {
        return !StringUtils.isNotEmpty(activityName) ||
                startTime == null || endTime == null ||
                !startTime.before(endTime);
    }
}
