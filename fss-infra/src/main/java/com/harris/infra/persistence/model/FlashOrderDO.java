package com.harris.infra.persistence.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class FlashOrderDO extends BaseDO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long itemId;
    private Long activityId;
    private String itemTitle;
    private Long flashPrice;
    private Integer quantity;
    private Long totalAmount;
    private Integer status;
    private Long userId;
}
