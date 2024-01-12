package com.harris.infra.persistence.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class FlashItemDO extends BaseDO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String itemTitle;
    private String itemSubTitle;
    private String itemDesc;
    private Integer initialStock;
    private Integer availableStock;
    private Integer stockWarmUp;
    private Long originalPrice;
    private Long flashPrice;
    private String rules;
    private Integer status;
    private Long activityId;
    private Date startTime;
    private Date endTime;
}
