package com.harris.app.model.result;

import com.harris.app.exception.AppErrCode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class AppResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String code;
    private String msg;

    public static AppResult ok() {
        return new AppResult().setSuccess(true);
    }

    public static AppResult error(AppErrCode appErrCode) {
        return new AppResult()
                .setSuccess(false)
                .setCode(appErrCode.getErrCode())
                .setMsg(appErrCode.getErrDesc());
    }
}
