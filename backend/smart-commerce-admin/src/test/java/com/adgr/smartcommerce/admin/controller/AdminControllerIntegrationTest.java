package com.adgr.smartcommerce.admin.controller;

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
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminEndpointShouldAllowAdmin() throws Exception {
        String token = login("admin", "Admin@123");

        MockHttpServletResponse response = mockMvc.perform(get("/api/admin/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        JsonNode json = objectMapper.readTree(response.getContentAsString());
        assertThat(json.path("code").asInt()).isEqualTo(0);
        assertThat(json.path("data").path("username").asText()).isEqualTo("admin");
    }

    @Test
    void adminEndpointShouldRejectCustomer() throws Exception {
        String token = login("alice", "Admin@123");

        mockMvc.perform(get("/api/admin/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String login(String username, String password) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        JsonNode json = objectMapper.readTree(response.getContentAsString());
        return json.path("data").path("accessToken").asText();
    }
}
