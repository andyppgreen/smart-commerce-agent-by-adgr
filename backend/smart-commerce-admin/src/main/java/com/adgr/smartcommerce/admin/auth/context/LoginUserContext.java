package com.adgr.smartcommerce.admin.auth.context;

import com.adgr.smartcommerce.admin.auth.dto.LoginUserPrincipal;

public final class LoginUserContext {

    private static final ThreadLocal<LoginUserPrincipal> CURRENT_USER = new ThreadLocal<>();

    private LoginUserContext() {
    }

    public static void set(LoginUserPrincipal principal) {
        CURRENT_USER.set(principal);
    }

    public static LoginUserPrincipal get() {
        return CURRENT_USER.get();
    }

    public static LoginUserPrincipal require() {
        LoginUserPrincipal principal = CURRENT_USER.get();
        if (principal == null) {
            throw new IllegalStateException("no current user in context");
        }
        return principal;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
