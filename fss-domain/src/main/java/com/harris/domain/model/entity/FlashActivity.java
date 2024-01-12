package com.harris.domain.model.entity;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;

@Data
public class FlashActivity implements Serializable {
    private Long id;
    private String activityName;
    private String activityDesc;
    private Integer status;
    private Date startTime;
    private Date endTime;

    public boolean invalidParams() {
        return StringUtils.isEmpty(activityName) ||
                startTime == null || endTime == null ||
                endTime.before(startTime) || endTime.before(new Date());
    }
}
