package com.adgr.smartcommerce.admin.auth.dto;

import java.time.Instant;

public record LoginToken(String accessToken, Instant expiresAt) {
}
