package com.harris.app.service.app.impl;

import com.harris.app.service.app.SecurityService;
import org.springframework.stereotype.Service;

@Service
public class GeneralSecurityService implements SecurityService {
    @Override
    public boolean inspectRisksByPolicy(Long userId) {
        return true;
    }
}
