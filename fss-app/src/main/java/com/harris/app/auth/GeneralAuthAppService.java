package com.harris.app.auth;

import com.alibaba.fastjson.JSON;
import com.harris.app.auth.model.AuthResult;
import com.harris.app.auth.model.AuthToken;
import com.harris.app.auth.model.ResourceEnum;
import com.harris.infra.controller.exception.AuthErrCode;
import com.harris.infra.controller.exception.AuthException;
import com.harris.infra.util.Base64Util;
import org.springframework.stereotype.Service;

@Service
public class GeneralAuthAppService implements AuthAppService {
    @Override
    public AuthResult auth(String encryptedToken) {
        AuthToken authToken = parseToken(encryptedToken);
        if (authToken == null) {
            throw new AuthException(AuthErrCode.INVALID_TOKEN);
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
            throw new AuthException(AuthErrCode.INVALID_TOKEN);
        }
    }
}
