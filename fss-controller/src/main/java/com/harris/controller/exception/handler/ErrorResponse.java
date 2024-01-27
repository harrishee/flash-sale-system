package com.harris.controller.exception.handler;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ErrorResponse {
    private String errCode;
    private String errMessage;
}
