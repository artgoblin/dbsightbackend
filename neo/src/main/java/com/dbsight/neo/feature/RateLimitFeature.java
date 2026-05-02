package com.dbsight.neo.feature;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimitFeature {
    
    private final StringRedisTemplate redisTemplate;

    private static final int DAILY_LIMIT = 5;

    public void checkLimit(Long userId) {
        String key = "rate_limit:" + userId;

        Long count = redisTemplate.opsForValue().increment(key);


        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofDays(1));
        }

        if (count != null && count > DAILY_LIMIT) {
            throw new RuntimeException("Daily AI limit reached (5 requests)");
        }
    }
}
