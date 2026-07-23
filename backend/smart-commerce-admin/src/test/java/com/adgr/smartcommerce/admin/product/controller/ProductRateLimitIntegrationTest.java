package com.adgr.smartcommerce.admin.product.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adgr.smartcommerce.admin.common.ratelimit.RedisRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class ProductRateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RedisRateLimiter redisRateLimiter;

    @Test
    void shouldKeepProductEndpointAvailableWhenRequestIsAllowed() throws Exception {
        String clientIp = "203.0.113.20";
        when(redisRateLimiter.tryAcquire(clientIp))
                .thenReturn(RedisRateLimiter.RateLimitDecision.allow());

        mockMvc.perform(get("/api/products/1").with(remoteAddress(clientIp)))
                .andExpect(status().isOk());

        verify(redisRateLimiter).tryAcquire(eq(clientIp));
    }

    @Test
    void shouldReturnTooManyRequestsWhenLimitIsExceeded() throws Exception {
        String clientIp = "203.0.113.21";
        when(redisRateLimiter.tryAcquire(clientIp))
                .thenReturn(RedisRateLimiter.RateLimitDecision.denied(8));

        mockMvc.perform(get("/api/products/1").with(remoteAddress(clientIp)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "8"))
                .andExpect(jsonPath("$.code").value(42900));

        verify(redisRateLimiter).tryAcquire(eq(clientIp));
    }

    private RequestPostProcessor remoteAddress(String remoteAddress) {
        return request -> {
            request.setRemoteAddr(remoteAddress);
            return request;
        };
    }
}
