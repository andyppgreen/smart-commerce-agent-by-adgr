package com.adgr.smartcommerce.admin.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adgr.smartcommerce.admin.auth.dto.LoginResponse;
import com.adgr.smartcommerce.admin.auth.dto.LoginUserInfo;
import com.adgr.smartcommerce.admin.auth.service.AuthService;
import com.adgr.smartcommerce.admin.auth.service.JwtTokenService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @Test
    void loginShouldReturnToken() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse(
                "token-123",
                "Bearer",
                Instant.parse("2026-07-05T00:00:00Z"),
                new LoginUserInfo(1L, "admin", "后台管理员", 1, List.of("ADMIN"))));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessToken").value("token-123"))
                .andExpect(jsonPath("$.data.user.username").value("admin"))
                .andExpect(jsonPath("$.data.user.roles[0]").value("ADMIN"));

        verify(authService).login(any());
    }
}
