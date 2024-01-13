package com.harris.app.model.result;

import com.harris.app.exception.AppErrCode;
import lombok.Data;

import static com.harris.app.exception.AppErrCode.TRY_LATER;

@Data
public class AppSingleResult<T> {
    private boolean success;
    private String code;
    private String msg;
    private T data;

    public static <T> AppSingleResult<T> ok(T data) {
        AppSingleResult<T> appSingleResult = new AppSingleResult<>();
        appSingleResult.setSuccess(true);
        appSingleResult.setData(data);
        return appSingleResult;
    }

    public static <T> AppSingleResult<T> error(String errCode, String errDesc) {
        AppSingleResult<T> appSingleResult = new AppSingleResult<>();
        appSingleResult.setSuccess(false);
        appSingleResult.setCode(errCode);
        appSingleResult.setMsg(errDesc);
        return appSingleResult;
    }

    public static <T> AppSingleResult<T> error(String errCode, String errDesc, T data) {
        AppSingleResult<T> appSingleResult = new AppSingleResult<>();
        appSingleResult.setSuccess(false);
        appSingleResult.setData(data);
        appSingleResult.setCode(errCode);
        appSingleResult.setMsg(errDesc);
        return appSingleResult;
    }

    public static <T> AppSingleResult<T> error(AppErrCode appErrorCode) {
        AppSingleResult<T> appSingleResult = new AppSingleResult<>();
        appSingleResult.setSuccess(false);
        appSingleResult.setCode(appErrorCode.getErrCode());
        appSingleResult.setMsg(appErrorCode.getErrDesc());
        return appSingleResult;
    }

    public static <T> AppSingleResult<T> tryLater() {
        AppSingleResult<T> appSingleResult = new AppSingleResult<>();
        appSingleResult.setSuccess(false);
        appSingleResult.setCode(TRY_LATER.getErrCode());
        appSingleResult.setMsg(TRY_LATER.getErrDesc());
        return appSingleResult;
    }
}
