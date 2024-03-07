package com.harris.app.service.auth;

import com.harris.app.model.auth.AuthResult;
import com.harris.app.model.ResourceEnum;

public interface AuthService {
    AuthResult auth(String encryptedToken);

    AuthResult auth(Long userId, ResourceEnum resourceEnum);
}
