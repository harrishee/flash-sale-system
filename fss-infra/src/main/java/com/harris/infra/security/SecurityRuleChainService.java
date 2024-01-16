package com.harris.infra.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface SecurityRuleChainService {
    boolean run(HttpServletRequest request, HttpServletResponse response);

    int getOrder();
}
