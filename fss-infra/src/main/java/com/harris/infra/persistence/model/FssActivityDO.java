package com.harris.infra.persistence.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @author: harris
 * @summary: flash-sale-system
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FssActivityDO extends BaseDO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String activityName;
    private Date startTime;
    private Date endTime;
    private Integer status;
    private Date modifiedTime;
    private Date createTime;
    private String activityDesc;
}
