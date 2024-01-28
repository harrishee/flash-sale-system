package com.harris.app.service.app.impl;

import com.alibaba.fastjson.JSON;
import com.harris.app.model.auth.AuthResult;
import com.harris.app.model.auth.AuthToken;
import com.harris.app.model.auth.ResourceEnum;
import com.harris.app.service.app.AuthAppService;
import com.harris.infra.controller.exception.AuthErrorCode;
import com.harris.infra.controller.exception.AuthException;
import com.harris.infra.util.Base64Util;
import org.springframework.stereotype.Service;

@Service
public class GeneralAuthAppService implements AuthAppService {
    @Override
    public AuthResult auth(String encryptedToken) {
        // Parse the token using Base64
        AuthToken authToken = parseToken(encryptedToken);
        if (authToken == null) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        // Return the pass result with user ID
        return new AuthResult().setUserId(authToken.getUserId()).pass();
    }

    @Override
    public AuthResult auth(Long userId, ResourceEnum resourceEnum) {
        // Return the pass result with user ID
        return new AuthResult().setUserId(userId).pass();
    }

    private AuthToken parseToken(String encryptedToken) {
        try {
            // Decode using Base64 and then parse into a JSON object
            String parsedToken = Base64Util.decode(encryptedToken);
            return JSON.parseObject(parsedToken, AuthToken.class);
        } catch (Exception e) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
    }
}
