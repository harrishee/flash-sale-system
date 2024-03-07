package com.harris.app.model;

import lombok.Getter;

@Getter
public enum ResourceEnum {
    ACTIVITY_CREATE("ACTIVITY_CREATE", "创建活动"),
    ACTIVITY_MODIFICATION("ACTIVITY_MODIFICATION", "活动修改"),
    ITEM_CREATE("ITEM_CREATE", "创建抢购品"),
    ITEM_MODIFICATION("ITEM_MODIFICATION", "抢购品修改");
    
    private final String code;
    private final String desc;
    
    ResourceEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
