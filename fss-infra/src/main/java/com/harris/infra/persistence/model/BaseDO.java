package com.harris.infra.persistence.model;

import lombok.Data;

import java.util.Date;

@Data
public class BaseDO {
    private Long id;
    private Date modifiedTime;
    private Date createTime;
}
