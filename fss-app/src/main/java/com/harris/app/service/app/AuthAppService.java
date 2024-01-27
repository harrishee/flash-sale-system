package com.harris.app.service.app;

import com.harris.app.model.auth.AuthResult;
import com.harris.app.model.auth.ResourceEnum;

public interface AuthAppService {
    AuthResult auth(String encryptedToken);

    AuthResult auth(Long userId, ResourceEnum resourceEnum);
}
