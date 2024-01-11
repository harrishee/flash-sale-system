package com.harris.domain.model.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author: harris
 * @summary: flash-sale-system
 */
@Data
public class FssActivity implements Serializable {
    private Long Id;
    private String activityName;
    private Date startTime;
    private Date endTime;
    private Integer status;
    private String activityDesc;
}
