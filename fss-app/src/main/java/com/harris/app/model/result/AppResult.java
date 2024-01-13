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
    private String errCode;
    private String errMsg;

    public static AppResult success() {
        AppResult appResult = new AppResult();
        appResult.setSuccess(true);
        return appResult;
    }

    public static AppResult fail(AppErrCode appErrCode) {
        AppResult appResult = new AppResult();
        appResult.setSuccess(false);
        appResult.setErrCode(appErrCode.getErrCode());
        appResult.setErrMsg(appErrCode.getErrDesc());
        return appResult;
    }
}
