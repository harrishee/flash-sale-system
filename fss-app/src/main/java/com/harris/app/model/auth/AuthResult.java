package com.harris.app.model.auth;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AuthResult {
    private Long userId;
    private boolean success;
    private String message;
    
    public AuthResult pass() {
        this.success = true;
        return this;
    }
}
