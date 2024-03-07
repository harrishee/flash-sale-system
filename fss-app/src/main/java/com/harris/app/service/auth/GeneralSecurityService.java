package com.harris.app.service.auth;

import com.harris.app.service.auth.SecurityService;
import org.springframework.stereotype.Service;

@Service
public class GeneralSecurityService implements SecurityService {
    @Override
    public boolean inspectRisksByPolicy(Long userId) {
        return true;
    }
}
