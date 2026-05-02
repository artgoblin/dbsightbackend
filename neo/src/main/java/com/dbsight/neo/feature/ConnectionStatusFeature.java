package com.dbsight.neo.feature;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConnectionStatusFeature {
    private final StringRedisTemplate redisTemplate;

    public String connectionStatus(String databaseName,Long userId) {
        String isDbPresent = redisTemplate.opsForValue()
                .get("dbConnections::" + userId + ":" + databaseName);

        if (isDbPresent != null && isDbPresent.length() != 0) {
            return "Connected";
        } else {
            return "Disconnected";
        }

    }
}
