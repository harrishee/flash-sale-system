package com.harris.app.auth.model;

import lombok.Getter;

@Getter
public enum ResourceEnum {
    ACTIVITY_CREATE("ACTIVITY_CREATE", "创建活动"),
    ACTIVITY_MODIFICATION("ACTIVITY_MODIFICATION", "活动修改"),
    ITEM_CREATE("ITEM_CREATE", "创建抢购品"),
    ITEM_MODIFICATION("ITEM_MODIFICATION", "抢购品修改"),
    STOCK_BUCKETS_ARRANGEMENT("STOCK_BUCKETS_ARRANGEMENT", "编排库存分桶"),
    STOCK_BUCKETS_SUMMERY_QUERY("STOCK_BUCKETS_SUMMERY_QUERY", "获取库存分桶");

    private final String code;
    private final String desc;

    ResourceEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
