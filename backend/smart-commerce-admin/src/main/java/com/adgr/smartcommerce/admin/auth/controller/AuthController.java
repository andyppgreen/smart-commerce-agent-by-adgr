package com.adgr.smartcommerce.admin.auth.controller;

import com.adgr.smartcommerce.admin.auth.dto.LoginRequest;
import com.adgr.smartcommerce.admin.auth.dto.LoginResponse;
import com.adgr.smartcommerce.admin.auth.dto.LoginUserInfo;
import com.adgr.smartcommerce.admin.auth.service.AuthService;
import com.adgr.smartcommerce.admin.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<LoginUserInfo> currentUser() {
        return ApiResponse.success(authService.currentUser());
    }
}
