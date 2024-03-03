package com.harris.infra.security;

/**
 * 基于滑动窗口算法的限流服务
 */
public interface SlidingWindowLimitService {
    /**
     * 使用Redis实现的基于滑动窗口算法的限流。
     *
     * @param userActionKey 表示用户和行为的键
     * @param period        滑动窗口的周期（毫秒）
     * @param size          在滑动窗口内允许的最大行为数
     * @return 如果请求在速率限制内，则为true；否则为false
     */
    boolean pass(String userActionKey, int period, int size);
}
