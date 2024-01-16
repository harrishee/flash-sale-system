package com.harris.app.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class FlashOrderDTO {
    private Long id;
    private Long itemId;
    private Long activityId;
    private Integer quantity;
    private Long totalAmount;
    private Integer status;
    private Long userId;
    private Date createTime;
}
