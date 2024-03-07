package com.harris.infra.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用于执行安全规则链的服务，演示作用：
 * - run 方法返回 true，表示安全检查通过
 * - getOrder 方法返回 0，表示优先级顺序
 */
public interface SecurityChainService {
    boolean run(HttpServletRequest request, HttpServletResponse response);
    
    int getPriority();
}
