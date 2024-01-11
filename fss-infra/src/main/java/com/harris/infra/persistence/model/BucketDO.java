package com.harris.infra.persistence.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author: harris
 * @summary: flash-sale-system
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BucketDO extends BaseDO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long itemId;
    private Integer totalStocksAmount;
    private Integer availableStocksAmount;
    private Integer serialNo;
    private Integer status;
}
