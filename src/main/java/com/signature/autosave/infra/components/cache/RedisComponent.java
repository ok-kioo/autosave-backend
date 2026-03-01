package com.signature.autosave.infra.components.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisComponent implements ICacheComponent {

    private final StringRedisTemplate redisTemplate;

    @Override
    public String processIdempotentRequest(String key) {

        String redisKey = "idempotency:" + key;

        Boolean created = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "Request processed", Duration.ofDays(1));

        if (Boolean.FALSE.equals(created)) {
            return "Request processed";
        }

        return null;
    }
}
