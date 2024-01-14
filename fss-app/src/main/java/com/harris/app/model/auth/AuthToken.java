package com.harris.app.model.auth;

import lombok.Data;

@Data
public class AuthToken {
    private Long userId;
    private String expireDate;
}
