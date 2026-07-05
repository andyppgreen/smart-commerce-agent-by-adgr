package com.adgr.smartcommerce.admin.auth.dto;

import java.util.List;

public record LoginUserInfo(
        Long id,
        String username,
        String nickname,
        Integer userType,
        List<String> roles) {
}
