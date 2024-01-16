package com.harris.controller.model.converter;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.harris.app.model.result.AppMultiResult;
import com.harris.app.model.result.AppResult;
import com.harris.app.model.result.AppSingleResult;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseConverter {
    public static Response with(AppResult appResult) {
        if (appResult == null) {
            return new Response();
        }
        Response response = new Response();
        response.setSuccess(appResult.isSuccess());
        response.setErrCode(appResult.getCode());
        response.setErrMessage(appResult.getMsg());
        return response;
    }

    public static <T> SingleResponse<T> withSingle(AppSingleResult<T> appResult) {
        if (appResult == null) {
            return new SingleResponse<>();
        }
        SingleResponse<T> singleResponse = new SingleResponse<>();
        singleResponse.setSuccess(appResult.isSuccess());
        singleResponse.setErrCode(appResult.getCode());
        singleResponse.setErrMessage(appResult.getMsg());
        singleResponse.setData(appResult.getData());
        return singleResponse;
    }

    public static <T> MultiResponse<T> withMulti(AppMultiResult<T> appResult) {
        if (appResult == null) {
            return new MultiResponse<>();
        }
        MultiResponse<T> multiResponse = new MultiResponse<>();
        multiResponse.setSuccess(appResult.isSuccess());
        multiResponse.setErrCode(appResult.getCode());
        multiResponse.setErrMessage(appResult.getMsg());
        multiResponse.setData(appResult.getData());
        return multiResponse;
    }
}
