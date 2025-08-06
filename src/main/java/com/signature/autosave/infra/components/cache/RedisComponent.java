package com.signature.autosave.infra.components.cache;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.UnifiedJedis;

@Component
public class RedisComponent {
    private final UnifiedJedis jedis = run();

    private UnifiedJedis run() {
        JedisClientConfig config = DefaultJedisClientConfig.builder()
                .user(System.getenv("REDIS_USER"))
                .password(System.getenv("REDIS_PASSWORD"))
                .build();

        return new UnifiedJedis(
                new HostAndPort(System.getenv("REDIS_HOST"), Integer.parseInt(System.getenv("REDIS_PORT"))),
                config
        );
    }

    public String processIdempotentRequest(String key) {
        String redisKey = "idempotency:" + key;

        String cached = jedis.get(redisKey);
        if (cached != null) {
            return cached;
        }

        // Store the response in Redis with a TTL of 1 day
        jedis.setex(redisKey, 86400, "Request processed");

        return null;
    }

    @PreDestroy
    public void close() {
        jedis.close();
    }
}
