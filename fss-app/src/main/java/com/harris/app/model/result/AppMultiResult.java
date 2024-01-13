package com.harris.app.model.result;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

import static com.harris.app.exception.AppErrCode.TRY_LATER;

@Data
@EqualsAndHashCode(callSuper = true)
public class AppMultiResult<T> extends AppResult {
    private Integer total;
    private Collection<T> data;

    public static <T> AppMultiResult<T> of(Integer total, Collection<T> data) {
        AppMultiResult<T> appMultiResult = new AppMultiResult<>();
        appMultiResult.setSuccess(true);
        appMultiResult.setTotal(total);
        appMultiResult.setData(data);
        return appMultiResult;
    }

    public static <T> AppMultiResult<T> tryLater() {
        AppMultiResult<T> multiResult = new AppMultiResult<>();
        multiResult.setSuccess(false);
        multiResult.setErrCode(TRY_LATER.getErrCode());
        multiResult.setErrMsg(TRY_LATER.getErrDesc());
        return multiResult;
    }

    public static <T> AppMultiResult<T> empty() {
        AppMultiResult<T> appMultiResult = new AppMultiResult<>();
        appMultiResult.setSuccess(false);
        return appMultiResult;
    }
}
