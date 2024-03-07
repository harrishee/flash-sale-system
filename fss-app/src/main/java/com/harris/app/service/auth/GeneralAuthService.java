package com.harris.app.service.auth;

import com.alibaba.fastjson.JSON;
import com.harris.app.model.auth.AuthResult;
import com.harris.app.model.auth.AuthToken;
import com.harris.app.model.ResourceEnum;
import com.harris.app.service.auth.AuthService;
import com.harris.infra.controller.exception.AuthErrorCode;
import com.harris.infra.controller.exception.AuthException;
import com.harris.infra.util.Base64Util;
import org.springframework.stereotype.Service;

@Service
public class GeneralAuthService implements AuthService {
    // 通过 token 进行鉴权
    @Override
    public AuthResult auth(String encryptedToken) {
        // 解析 token
        AuthToken authToken = parseToken(encryptedToken);
        if (authToken == null) throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        
        // 设置 userId 和通过标志 到 AuthResult 对象并返回
        return new AuthResult().setUserId(authToken.getUserId()).pass();
    }
    
    // 通过 userId 和资源类型进行鉴权
    @Override
    public AuthResult auth(Long userId, ResourceEnum resourceEnum) {
        // 设置 userId 和通过标志 到 AuthResult 对象并返回
        return new AuthResult().setUserId(userId).pass();
    }
    
    private AuthToken parseToken(String encryptedToken) {
        try {
            // 用 Base64 解析 token，然后用 JSON 解析成 AuthToken 对象
            String parsedToken = Base64Util.decode(encryptedToken);
            return JSON.parseObject(parsedToken, AuthToken.class);
        } catch (Exception e) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
    }
}
