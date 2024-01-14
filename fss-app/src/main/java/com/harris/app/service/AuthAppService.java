package com.harris.app.service;

import com.harris.app.model.auth.AuthResult;
import com.harris.app.model.enums.ResourceEnum;

public interface AuthAppService {
    AuthResult auth(String encryptedToken);

    AuthResult auth(Long userId, ResourceEnum resourceEnum);
}
