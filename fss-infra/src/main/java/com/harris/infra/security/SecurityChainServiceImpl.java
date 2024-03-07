package com.harris.infra.security;

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
    public int getPriority() {
        return 0;
    }
}
