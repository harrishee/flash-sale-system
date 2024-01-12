package com.harris.domain.model.entity;

import lombok.Data;

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
}
