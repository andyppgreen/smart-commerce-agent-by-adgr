package com.adgr.smartcommerce.admin.auth.interceptor;

import com.adgr.smartcommerce.admin.auth.annotation.RequireRoles;
import com.adgr.smartcommerce.admin.auth.context.LoginUserContext;
import com.adgr.smartcommerce.admin.auth.dto.LoginUserPrincipal;
import com.adgr.smartcommerce.admin.common.exception.BusinessException;
import com.adgr.smartcommerce.admin.common.response.ResultCode;
import java.util.Arrays;
import java.util.List;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RoleAuthorizationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(jakarta.servlet.http.HttpServletRequest request,
                             jakarta.servlet.http.HttpServletResponse response,
                             Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRoles requireRoles = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), RequireRoles.class);
        if (requireRoles == null) {
            requireRoles = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getBeanType(), RequireRoles.class);
        }
        if (requireRoles == null) {
            return true;
        }

        LoginUserPrincipal principal = LoginUserContext.require();
        List<String> roles = principal.roles();
        boolean allowed = Arrays.stream(requireRoles.value())
                .anyMatch(roles::contains);
        if (!allowed) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return true;
    }
}
