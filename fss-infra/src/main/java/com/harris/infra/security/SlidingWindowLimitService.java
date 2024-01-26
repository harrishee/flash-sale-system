package com.harris.infra.security;

/**
 * Service for rate limiting based on a sliding window algorithm.
 */
public interface SlidingWindowLimitService {
    /**
     * Implements rate limiting based on a sliding window algorithm using Redis.
     *
     * @param userActionKey The key identifying the user and action
     * @param period        The period (in milliseconds) over which to spread the sliding window
     * @param size          The maximum number of allowed actions within the sliding window
     * @return true if the request is within rate limits, false otherwise
     */
    boolean pass(String userActionKey, int period, int size);
}
