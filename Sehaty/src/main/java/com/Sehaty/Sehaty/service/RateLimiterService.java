package com.Sehaty.Sehaty.service;


import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for rate limiting requests.
 * Uses a token bucket algorithm to limit requests per user or IP.
 */
@Service
public class RateLimiterService {

    private final Map<String, UserBucket> buckets = new ConcurrentHashMap<>();

    @Value("${rate.limit.requests}")
    private int LIMIT;

    @Value("${rate.limit.window-ms}")
    private long WINDOW;

    /**
     * Checks if a request is allowed for the given user ID.
     *
     * @param userId The user ID or IP address.
     * @return true if the request is allowed, false otherwise.
     */
    public synchronized boolean allowRequest(String userId) {
        long now = System.currentTimeMillis();

        buckets.putIfAbsent(userId, new UserBucket(0, now + WINDOW));
        UserBucket bucket = buckets.get(userId);

        if (now > bucket.resetTime) {
            bucket.count = 0;
            bucket.resetTime = now + WINDOW;
        }

        bucket.count++;

        return bucket.count <= LIMIT;
    }

    private static class UserBucket {
        int count;
        long resetTime;

        UserBucket(int count, long resetTime) {
            this.count = count;
            this.resetTime = resetTime;
        }
    }
}
