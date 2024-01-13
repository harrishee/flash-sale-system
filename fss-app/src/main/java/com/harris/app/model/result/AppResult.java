package com.harris.app.model.result;

import com.harris.app.exception.AppErrCode;
import lombok.Data;

import java.io.Serializable;

@Data
public class AppResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String errCode;
    private String errMsg;

    public static AppResult ok() {
        AppResult appResult = new AppResult();
        appResult.setSuccess(true);
        return appResult;
    }

    public static AppResult error(AppErrCode appErrCode) {
        AppResult appResult = new AppResult();
        appResult.setSuccess(false);
        appResult.setErrCode(appErrCode.getErrCode());
        appResult.setErrMsg(appErrCode.getErrDesc());
        return appResult;
    }
}
