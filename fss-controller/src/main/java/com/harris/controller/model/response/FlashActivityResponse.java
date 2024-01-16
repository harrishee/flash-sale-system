package com.harris.controller.model.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class FlashActivityResponse {
    private Long id;
    private String activityName;
    private String activityDesc;
    private Integer status;
    private Date startTime;
    private Date endTime;
    private Long version;
}
