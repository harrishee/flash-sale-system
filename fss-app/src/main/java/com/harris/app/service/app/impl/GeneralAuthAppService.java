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
        // Parse the token
        AuthToken authToken = parseToken(encryptedToken);

        // Check if the token is valid
        if (authToken == null) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        return new AuthResult().setUserId(authToken.getUserId()).pass();
    }

    /**
     * Authenticate based on user ID and a specific resource.
     *
     * @param userId       User ID
     * @param resourceEnum Resource
     * @return AuthResult, including user ID and pass status
     */
    @Override
    public AuthResult auth(Long userId, ResourceEnum resourceEnum) {
        return new AuthResult().setUserId(userId).pass();
    }

    /**
     * Parse token using Base64
     *
     * @param encryptedToken encrypted token
     * @return Parsed AuthToken
     */
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
