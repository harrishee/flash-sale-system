package com.harris.app.auth;

import com.harris.app.auth.model.AuthResult;
import com.harris.app.auth.model.ResourceEnum;

public interface AuthAppService {
    AuthResult auth(String encryptedToken);

    AuthResult auth(Long userId, ResourceEnum resourceEnum);
}
