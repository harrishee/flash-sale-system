package com.harris.domain.exception;

import com.alibaba.cola.dto.ErrorCodeI;

public enum DomainErrCode implements ErrorCodeI {
    INVALID_PARAMS("INVALID_PARAMS", "参数错误"),
    ONLINE_ACTIVITY_INVALID_PARAMS("ONLINE_ACTIVITY_INVALID_PARAMS", "待上线活动参数无效"),
    ACTIVITY_DOES_NOT_EXIST("ACTIVITY_DOES_NOT_EXIST", "活动不存在"),
    ACTIVITY_OFFLINE("ACTIVITY_OFFLINE", "活动已下线"),
    ACTIVITY_NOT_ONLINE("ACTIVITY_NOT_ONLINE", "活动尚未上线"),
    ACTIVITY_NOT_IN_PROGRESS("ACTIVITY_NOT_IN_PROGRESS", "当前非活动时段"),
    ONLINE_ITEM_INVALID_PARAMS("ONLINE_ITEM_INVALID_PARAMS", "待上线商品参数无效"),
    ITEM_DOES_NOT_EXIST("ITEM_DOES_NOT_EXIST", "商品不存在"),
    ITEM_OFFLINE("ITEM_OFFLINE", "商品已下线"),
    ITEM_NOT_ONLINE("ITEM_NOT_ONLINE", "商品尚未上线"),
    ITEM_NOT_IN_PROGRESS("ITEM_NOT_IN_PROGRESS", "当前非商品时段"),
    PRIMARY_BUCKET_IS_MISSING("PRIMARY_BUCKET_IS_MISSING", "主桶缺失"),
    MULTI_PRIMARY_BUCKETS_FOUND_BUT_EXPECT_ONE("MULTI_PRIMARY_BUCKETS_FOUND_BUT_EXPECT_ONE", "发现多个主桶但只需要一个"),
    TOTAL_STOCKS_AMOUNT_INVALID("TOTAL_STOCKS_AMOUNT_INVALID", "库存总数错误"),
    AVAILABLE_STOCKS_AMOUNT_NOT_EQUALS_TO_TOTAL_STOCKS_AMOUNT("AVAILABLE_STOCKS_AMOUNT_NOT_EQUALS_TO_TOTAL_STOCKS_AMOUNT", "子桶可用库存与库存总数不匹配"),
    AVAILABLE_STOCKS_AMOUNT_INVALID("AVAILABLE_STOCKS_AMOUNT_INVALID", "子桶可用库存数量错误"),
    STOCK_BUCKET_ITEM_INVALID("STOCK_BUCKET_ITEM_INVALID", "商品ID设置错误");

    private final String errCode;
    private final String errDesc;

    DomainErrCode(String errCode, String errDesc) {
        this.errCode = errCode;
        this.errDesc = errDesc;
    }

    @Override
    public String getErrCode() {
        return errCode;
    }

    @Override
    public String getErrDesc() {
        return errDesc;
    }
}
