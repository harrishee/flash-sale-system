package com.harris.app.model.auth;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AuthResult {
    private boolean success;
    private Long userId;
    private String msg;

    public AuthResult pass() {
        this.success = true;
        return this;
    }
}
