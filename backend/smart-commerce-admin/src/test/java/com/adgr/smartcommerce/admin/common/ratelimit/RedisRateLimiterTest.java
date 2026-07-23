package com.adgr.smartcommerce.admin.common.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

class RedisRateLimiterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    private RateLimitProperties properties;
    private RedisRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        properties = new RateLimitProperties();
        properties.setMaxRequests(2);
        properties.setWindowSeconds(10);
        rateLimiter = new RedisRateLimiter(redisTemplate, properties);
    }

    @Test
    void shouldAllowRequestsWithinWindow() {
        when(redisTemplate.execute(
                any(RedisScript.class),
                anyList(),
                any(Object[].class))).thenReturn(2L);

        RedisRateLimiter.RateLimitDecision decision = rateLimiter.tryAcquire("127.0.0.1");

        assertThat(decision.allowed()).isTrue();
    }

    @Test
    void shouldDenyRequestsAfterLimit() {
        when(redisTemplate.execute(
                any(RedisScript.class),
                anyList(),
                any(Object[].class))).thenReturn(3L);

        RedisRateLimiter.RateLimitDecision decision = rateLimiter.tryAcquire("127.0.0.1");

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.retryAfterSeconds()).isEqualTo(10);
    }

    @Test
    void shouldAllowWhenRedisFails() {
        when(redisTemplate.execute(
                any(RedisScript.class),
                anyList(),
                any(Object[].class))).thenThrow(new RedisConnectionFailureException("down"));

        RedisRateLimiter.RateLimitDecision decision = rateLimiter.tryAcquire("127.0.0.1");

        assertThat(decision.allowed()).isTrue();
    }
}
