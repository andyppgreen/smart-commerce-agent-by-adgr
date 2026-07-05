package com.adgr.smartcommerce.admin.auth.dto;

import java.time.Instant;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        LoginUserInfo user) {
}
