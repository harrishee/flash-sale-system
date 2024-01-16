package com.harris.infra.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SecurityRuleChainServiceImpl implements SecurityRuleChainService {
    @Override
    public boolean run(HttpServletRequest request, HttpServletResponse response) {
        return true;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
