package com.emergencymed.chatbot.service;

import com.emergencymed.chatbot.config.ChatbotProperties;
import com.emergencymed.common.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatbotRateLimiter {

    private final ChatbotProperties properties;
    private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    public ChatbotRateLimiter(ChatbotProperties properties) {
        this.properties = properties;
    }

    /**
     * Checks whether the given key (user ID or IP) has exceeded the rate limit (20 msgs/min).
     * Throws ApiException with HTTP 429 if limit is exceeded.
     */
    public synchronized void checkRateLimit(String key) {
        if (key == null || key.isBlank()) {
            key = "anonymous";
        }

        int maxPerMinute = properties.getRateLimit().getPerMinute();
        long now = Instant.now().toEpochMilli();
        long windowStart = now - 60_000L; // 60 seconds

        Deque<Long> timestamps = requestLog.computeIfAbsent(key, k -> new ArrayDeque<>());

        // Evict timestamps older than 60 seconds
        while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= maxPerMinute) {
            throw new ApiException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "RATE_LIMIT_EXCEEDED",
                    "Rate limit exceeded. Please wait a moment before sending another message."
            );
        }

        timestamps.addLast(now);
    }

    /**
     * Resets rate limit for testing purposes.
     */
    public void reset() {
        requestLog.clear();
    }
}
