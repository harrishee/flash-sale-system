package com.harris.app.security.impl;

import com.harris.app.security.SecurityService;
import org.springframework.stereotype.Service;

@Service
public class GeneralSecurityService implements SecurityService {
    @Override
    public boolean inspectRisksByPolicy(Long userId) {
        return true;
    }
}
