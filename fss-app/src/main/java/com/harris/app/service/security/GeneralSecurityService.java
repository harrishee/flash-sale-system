package com.harris.app.service.security;

import org.springframework.stereotype.Service;

@Service
public class GeneralSecurityService implements SecurityService {
    @Override
    public boolean inspectRisksByPolicy(Long userId) {
        return true;
    }
}
