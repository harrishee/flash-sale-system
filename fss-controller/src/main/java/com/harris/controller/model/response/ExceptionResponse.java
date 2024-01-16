package com.harris.controller.model.response;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ExceptionResponse {
    private String errCode;
    private String errMsg;
}
