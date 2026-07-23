package com.adgr.smartcommerce.admin.common.ratelimit;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class RedisRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimiter.class);
    private static final String KEY_PREFIX = "ratelimit:api:products:";
    private static final DefaultRedisScript<Long> INCREMENT_SCRIPT = new DefaultRedisScript<>(
            "local current = redis.call('INCR', KEYS[1])\n"
                    + "if current == 1 then\n"
                    + "  redis.call('EXPIRE', KEYS[1], ARGV[1])\n"
                    + "end\n"
                    + "return current",
            Long.class);

    private final StringRedisTemplate redisTemplate;
    private final RateLimitProperties properties;

    public RedisRateLimiter(StringRedisTemplate redisTemplate, RateLimitProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public RateLimitDecision tryAcquire(String clientIp) {
        if (!properties.isEnabled()) {
            return RateLimitDecision.allow();
        }

        String key = KEY_PREFIX + clientIp;
        long maxRequests = Math.max(1, properties.getMaxRequests());
        long windowSeconds = Math.max(1, properties.getWindowSeconds());
        try {
            Long current = redisTemplate.execute(
                    INCREMENT_SCRIPT,
                    List.of(key),
                    String.valueOf(windowSeconds));
            if (current == null || current <= maxRequests) {
                return RateLimitDecision.allow();
            }
            return RateLimitDecision.denied(windowSeconds);
        } catch (RuntimeException ex) {
            log.warn("Rate limiter degraded to allow, key={}, reason={}: {}",
                    key, ex.getClass().getSimpleName(), ex.getMessage());
            return RateLimitDecision.allow();
        }
    }

    public record RateLimitDecision(boolean allowed, long retryAfterSeconds) {

        public static RateLimitDecision allow() {
            return new RateLimitDecision(true, 0);
        }

        public static RateLimitDecision denied(long retryAfterSeconds) {
            return new RateLimitDecision(false, Math.max(1, retryAfterSeconds));
        }
    }
}
