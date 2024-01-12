package com.harris.infra.persistence.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class FlashActivityDO extends BaseDO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String activityName;
    private String activityDesc;
    private Integer status;
    private Date startTime;
    private Date endTime;
}
