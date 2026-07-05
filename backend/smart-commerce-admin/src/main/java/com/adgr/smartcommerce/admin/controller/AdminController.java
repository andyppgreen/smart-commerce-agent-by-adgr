package com.adgr.smartcommerce.admin.controller;

import com.adgr.smartcommerce.admin.auth.annotation.RequireRoles;
import com.adgr.smartcommerce.admin.auth.dto.LoginUserInfo;
import com.adgr.smartcommerce.admin.auth.service.AuthService;
import com.adgr.smartcommerce.admin.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequireRoles("ADMIN")
public class AdminController {

    private final AuthService authService;

    public AdminController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public ApiResponse<LoginUserInfo> me() {
        return ApiResponse.success(authService.currentUser());
    }
}
