package com.adgr.smartcommerce.admin.common.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.adgr.smartcommerce.admin.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitInterceptorTest {

    private final RedisRateLimiter redisRateLimiter = mock(RedisRateLimiter.class);
    private final RateLimitInterceptor interceptor = new RateLimitInterceptor(redisRateLimiter);

    @Test
    void shouldAllowRequestWithinLimit() throws Exception {
        MockHttpServletRequest request = requestFrom("203.0.113.10");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(redisRateLimiter.tryAcquire("203.0.113.10"))
                .thenReturn(RedisRateLimiter.RateLimitDecision.allow());

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    void shouldRejectRequestAfterLimit() {
        MockHttpServletRequest request = requestFrom("203.0.113.11");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(redisRateLimiter.tryAcquire("203.0.113.11"))
                .thenReturn(RedisRateLimiter.RateLimitDecision.denied(7));

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getResultCode())
                .isEqualTo(com.adgr.smartcommerce.admin.common.response.ResultCode.RATE_LIMITED);
        assertThat(response.getHeader("Retry-After")).isEqualTo("7");
    }

    private MockHttpServletRequest requestFrom(String remoteAddress) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(remoteAddress);
        return request;
    }
}
