package com.adgr.smartcommerce.admin.config;

import com.adgr.smartcommerce.admin.auth.interceptor.JwtAuthInterceptor;
import com.adgr.smartcommerce.admin.auth.interceptor.RoleAuthorizationInterceptor;
import com.adgr.smartcommerce.admin.common.ratelimit.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final RoleAuthorizationInterceptor roleAuthorizationInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;

    public WebMvcConfig(JwtAuthInterceptor jwtAuthInterceptor,
                        RoleAuthorizationInterceptor roleAuthorizationInterceptor,
                        RateLimitInterceptor rateLimitInterceptor) {
        this.jwtAuthInterceptor = jwtAuthInterceptor;
        this.roleAuthorizationInterceptor = roleAuthorizationInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/products", "/api/products/**");
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/products/**",
                        "/api/system/health",
                        "/actuator/**",
                        "/error");
        registry.addInterceptor(roleAuthorizationInterceptor)
                .addPathPatterns("/api/**");
    }
}
