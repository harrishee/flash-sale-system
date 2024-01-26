package com.harris.infra.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Service for security rule chain execution.
 * Now it only returns true for run and 0 for order.
 */
public interface SecurityChainService {
    /**
     * Executes the security rule chain for the given request and response.
     *
     * @param request  The HTTP request
     * @param response The HTTP response
     * @return Execution result
     */
    boolean run(HttpServletRequest request, HttpServletResponse response);

    /**
     * Execution order of the security chain.
     *
     * @return Execution order
     */
    int getOrder();
}
