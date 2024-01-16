package com.harris.app.auth.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AuthToken {
    private Long userId;
    private String expireDate;
}
