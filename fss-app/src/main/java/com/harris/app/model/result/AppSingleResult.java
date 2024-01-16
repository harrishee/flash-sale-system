package com.harris.app.model.result;

import com.harris.app.exception.AppErrCode;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.harris.app.exception.AppErrCode.TRY_LATER;

@Data
@Accessors(chain = true)
public class AppSingleResult<T> {
    private boolean success;
    private String code;
    private String msg;
    private T data;

    public static <T> AppSingleResult<T> ok(T data) {
        return new AppSingleResult<T>().setSuccess(true).setData(data);
    }

    public static <T> AppSingleResult<T> error(String errCode, String errDesc) {
        return new AppSingleResult<T>()
                .setSuccess(false)
                .setCode(errCode)
                .setMsg(errDesc);
    }

    public static <T> AppSingleResult<T> error(String errCode, String errDesc, T data) {
        return new AppSingleResult<T>()
                .setSuccess(false)
                .setData(data)
                .setCode(errCode)
                .setMsg(errDesc);
    }

    public static <T> AppSingleResult<T> error(AppErrCode appErrorCode) {
        return new AppSingleResult<T>()
                .setSuccess(false)
                .setCode(appErrorCode.getErrCode())
                .setMsg(appErrorCode.getErrDesc());
    }

    public static <T> AppSingleResult<T> tryLater() {
        return new AppSingleResult<T>()
                .setSuccess(false)
                .setCode(TRY_LATER.getErrCode())
                .setMsg(TRY_LATER.getErrDesc());
    }
}
