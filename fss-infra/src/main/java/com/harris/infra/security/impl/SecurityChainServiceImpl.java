package com.harris.infra.security.impl;

import com.harris.infra.security.SecurityChainService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class SecurityChainServiceImpl implements SecurityChainService {
    @Override
    public boolean run(HttpServletRequest request, HttpServletResponse response) {
        return true;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
