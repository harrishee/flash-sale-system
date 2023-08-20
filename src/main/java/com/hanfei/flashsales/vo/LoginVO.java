package com.hanfei.flashsales.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    @NotNull
    @Length(min = 5)
    private String userId;

    @NotNull
    @Length(min = 32)
    private String password;
}
