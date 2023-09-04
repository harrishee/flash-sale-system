package com.hanfei.flashsales.exception;

import com.hanfei.flashsales.vo.ResultEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GlobalException class is used to encapsulate custom exceptions for the application
 *
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalException extends RuntimeException {

    private ResultEnum resultEnum;
}
