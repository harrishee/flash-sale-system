package com.harris.app.model.result;

import com.harris.app.exception.AppErrorCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

@Data
@EqualsAndHashCode(callSuper = true)
public class AppMultiResult<T> extends AppResult {
    private Integer total;
    private Collection<T> data;
    
    public static <T> AppMultiResult<T> of(Collection<T> data, Integer total) {
        AppMultiResult<T> appMultiResult = new AppMultiResult<>();
        appMultiResult.setSuccess(true);
        appMultiResult.setTotal(total);
        appMultiResult.setData(data);
        return appMultiResult;
    }
    
    public static <T> AppMultiResult<T> tryLater() {
        AppMultiResult<T> appMultiResult = new AppMultiResult<>();
        appMultiResult.setSuccess(false);
        appMultiResult.setCode(AppErrorCode.TRY_LATER.getErrCode());
        appMultiResult.setMessage(AppErrorCode.TRY_LATER.getErrDesc());
        return appMultiResult;
    }
    
    public static <T> AppMultiResult<T> empty() {
        AppMultiResult<T> appMultiResult = new AppMultiResult<>();
        appMultiResult.setSuccess(false);
        return appMultiResult;
    }
}
