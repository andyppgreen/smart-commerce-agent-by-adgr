package com.adgr.smartcommerce.admin.common.ratelimit;

import com.adgr.smartcommerce.admin.common.exception.BusinessException;
import com.adgr.smartcommerce.admin.common.response.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisRateLimiter redisRateLimiter;

    public RateLimitInterceptor(RedisRateLimiter redisRateLimiter) {
        this.redisRateLimiter = redisRateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientIp = StringUtils.hasText(request.getRemoteAddr())
                ? request.getRemoteAddr()
                : "unknown";
        RedisRateLimiter.RateLimitDecision decision = redisRateLimiter.tryAcquire(clientIp);
        if (!decision.allowed()) {
            response.setHeader("Retry-After", String.valueOf(decision.retryAfterSeconds()));
            throw new BusinessException(ResultCode.RATE_LIMITED);
        }
        return true;
    }
}
