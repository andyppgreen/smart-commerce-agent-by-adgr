package com.adgr.smartcommerce.admin.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class AuthCurrentUserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void currentUserShouldBeResolvedFromJwtToken() throws Exception {
        MockHttpServletResponse loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "Admin@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        JsonNode loginJson = objectMapper.readTree(loginResponse.getContentAsString());
        String token = loginJson.path("data").path("accessToken").asText();

        MockHttpServletResponse meResponse = mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        JsonNode meJson = objectMapper.readTree(meResponse.getContentAsString());
        assertThat(meJson.path("code").asInt()).isEqualTo(0);
        assertThat(meJson.path("data").path("username").asText()).isEqualTo("admin");
        assertThat(meJson.path("data").path("roles").get(0).asText()).isEqualTo("ADMIN");
    }

    @Test
    void currentUserShouldRejectMissingToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
