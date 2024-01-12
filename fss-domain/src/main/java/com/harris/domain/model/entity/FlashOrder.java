package com.harris.domain.model.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FlashOrder implements Serializable {
    private Long id;
    private Long itemId;
    private Long activityId;
    private String itemTitle;
    private Long flashPrice;
    private Integer quantity;
    private Long totalAmount;
    private Integer status;
    private Long userId;
    private Date createTime;
}
