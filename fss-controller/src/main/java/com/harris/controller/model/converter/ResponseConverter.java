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
    public static Response toResponse(AppResult appResult) {
        if (appResult == null) return new Response();
        Response response = new Response();
        response.setSuccess(appResult.isSuccess());
        response.setErrCode(appResult.getCode());
        response.setErrMessage(appResult.getMessage());
        return response;
    }
    
    public static <T> SingleResponse<T> toSingleResponse(AppSingleResult appSingleResult) {
        if (appSingleResult == null) return new SingleResponse<>();
        SingleResponse singleResponse = new SingleResponse();
        singleResponse.setSuccess(appSingleResult.isSuccess());
        singleResponse.setErrCode(appSingleResult.getCode());
        singleResponse.setErrMessage(appSingleResult.getMessage());
        singleResponse.setData(appSingleResult.getData());
        return singleResponse;
    }
    
    public static <T> MultiResponse<T> toMultiResponse(AppMultiResult appMultiResult) {
        if (appMultiResult == null) return new MultiResponse<>();
        MultiResponse multiResponse = new MultiResponse<>();
        multiResponse.setSuccess(appMultiResult.isSuccess());
        multiResponse.setErrCode(appMultiResult.getCode());
        multiResponse.setErrMessage(appMultiResult.getMessage());
        multiResponse.setData(appMultiResult.getData());
        return multiResponse;
    }
}
