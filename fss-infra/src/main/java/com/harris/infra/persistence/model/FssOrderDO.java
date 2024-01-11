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
public class FssOrderDO extends BaseDO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long itemId;
    private String itemTitle;
    private Long flashPrice;
    private Long activityId;
    private Integer quantity;
    private Long totalAmount;
    private Integer status;
    private Long userId;
}
