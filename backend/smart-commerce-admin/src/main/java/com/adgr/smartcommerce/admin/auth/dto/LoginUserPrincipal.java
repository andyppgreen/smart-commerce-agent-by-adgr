package com.adgr.smartcommerce.admin.auth.dto;

import java.util.List;

public record LoginUserPrincipal(Long userId, String username, Integer userType, List<String> roles) {
}
