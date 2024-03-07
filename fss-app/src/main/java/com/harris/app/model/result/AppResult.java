package com.harris.app.model.result;

import com.harris.app.exception.AppErrorCode;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AppResult {
    private boolean success;
    private String code;
    private String message;
    
    public static AppResult ok() {
        return new AppResult().setSuccess(true);
    }
    
    public static AppResult error(AppErrorCode appErrorCode) {
        return new AppResult()
                .setSuccess(false)
                .setCode(appErrorCode.getErrCode())
                .setMessage(appErrorCode.getErrDesc());
    }
}
