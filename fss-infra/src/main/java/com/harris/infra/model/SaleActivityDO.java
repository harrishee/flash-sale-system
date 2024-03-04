package com.harris.infra.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SaleActivityDO implements Serializable {
    private Long id;
    private String activityName;
    private String activityDesc;
    private Integer status;
    private Date startTime;
    private Date endTime;
    private Date modifiedTime;
    private Date createTime;
}
