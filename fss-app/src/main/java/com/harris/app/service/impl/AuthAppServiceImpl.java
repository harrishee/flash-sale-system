package com.harris.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.harris.app.model.auth.AuthResult;
import com.harris.app.model.auth.AuthToken;
import com.harris.app.model.enums.ResourceEnum;
import com.harris.app.service.AuthAppService;
import com.harris.infra.controller.exception.AuthException;
import com.harris.infra.util.Base64Util;

import static com.harris.infra.controller.exception.AuthErrCode.INVALID_TOKEN;

public class AuthAppServiceImpl implements AuthAppService {
    @Override
    public AuthResult auth(String encryptedToken) {
        AuthToken authToken = parseToken(encryptedToken);
        if (authToken == null) {
            throw new AuthException(INVALID_TOKEN);
        }
        return new AuthResult().setUserId(authToken.getUserId()).pass();
    }

    @Override
    public AuthResult auth(Long userId, ResourceEnum resourceEnum) {
        return new AuthResult().setUserId(userId).pass();
    }

    private AuthToken parseToken(String encryptedToken) {
        try {
            String parsedToken = Base64Util.decode(encryptedToken);
            return JSON.parseObject(parsedToken, AuthToken.class);
        } catch (Exception e) {
            throw new AuthException(INVALID_TOKEN);
        }
    }
}
