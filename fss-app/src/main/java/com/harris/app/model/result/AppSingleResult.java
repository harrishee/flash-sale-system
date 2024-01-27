package com.harris.app.model.result;

import com.harris.app.exception.AppErrorCode;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AppSingleResult<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;

    public static <T> AppSingleResult<T> ok(T data) {
        return new AppSingleResult<T>().setSuccess(true).setData(data);
    }

    public static <T> AppSingleResult<T> error(String errCode, String errDesc) {
        return new AppSingleResult<T>()
                .setSuccess(false)
                .setCode(errCode)
                .setMessage(errDesc);
    }

    public static <T> AppSingleResult<T> error(String errCode, String errDesc, T data) {
        return new AppSingleResult<T>()
                .setSuccess(false)
                .setData(data)
                .setCode(errCode)
                .setMessage(errDesc);
    }

    public static <T> AppSingleResult<T> error(AppErrorCode appErrorCode) {
        return new AppSingleResult<T>()
                .setSuccess(false)
                .setCode(appErrorCode.getErrCode())
                .setMessage(appErrorCode.getErrDesc());
    }

    public static <T> AppSingleResult<T> tryLater() {
        return new AppSingleResult<T>()
                .setSuccess(false)
                .setCode(AppErrorCode.TRY_LATER.getErrCode())
                .setMessage(AppErrorCode.TRY_LATER.getErrDesc());
    }
}
