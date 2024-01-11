package com.harris.domain.model.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author: harris
 * @summary: flash-sale-system
 */
@Data
public class FssOrder implements Serializable {
    private Long id;
    private Long itemId;
    private String itemTitle;
    private Long flashPrice;
    private Long activityId;
    private Integer quantity;
    private Long totalAmount;
    private Integer status;
    private Long userId;
    private Date createTime;
}
