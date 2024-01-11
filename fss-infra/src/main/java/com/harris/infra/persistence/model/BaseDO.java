package com.harris.infra.persistence.model;

import lombok.Data;

import java.util.Date;

/**
 * @author: harris
 * @summary: flash-sale-system
 */
@Data
public class BaseDO {
    private Long id;
    private Date createTime;
    private Date modifiedTime;
}
