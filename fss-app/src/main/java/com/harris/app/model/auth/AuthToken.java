package com.harris.app.model.auth;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AuthToken {
    private Long userId;
    private String expireDate;
}
