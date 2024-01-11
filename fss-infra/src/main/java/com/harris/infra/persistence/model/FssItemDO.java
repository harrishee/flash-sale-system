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
public class FssItemDO extends BaseDO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String itemTitle;
    private String itemSubTitle;
    private Integer initialStock;
    private Integer availableStock;
    private Integer stockWarmUp;
    private Long originalPrice;
    private Long flashPrice;
    private Date startTime;
    private Date endTime;
    private Integer status;
    private Long activityId;
    private String itemDesc;
    private String rules;
}
