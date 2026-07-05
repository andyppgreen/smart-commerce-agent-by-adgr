package com.adgr.smartcommerce.admin.auth.interceptor;

import com.adgr.smartcommerce.admin.auth.context.LoginUserContext;
import com.adgr.smartcommerce.admin.auth.dto.LoginUserPrincipal;
import com.adgr.smartcommerce.admin.auth.service.JwtTokenService;
import com.adgr.smartcommerce.admin.common.exception.BusinessException;
import com.adgr.smartcommerce.admin.common.response.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtTokenService jwtTokenService;

    public JwtAuthInterceptor(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        String token = authHeader.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        try {
            LoginUserPrincipal principal = jwtTokenService.parseToken(token);
            LoginUserContext.set(principal);
        } catch (RuntimeException ex) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUserContext.clear();
    }
}
