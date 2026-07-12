package com.adgr.smartcommerce.admin.category.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adgr.smartcommerce.admin.category.service.ProductCategoryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
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
class ProductCategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductCategoryService productCategoryService;

    private Long createdCategoryId;

    @AfterEach
    void cleanUp() {
        if (createdCategoryId == null) {
            return;
        }
        var createdCategory = productCategoryService.getById(createdCategoryId);
        if (createdCategory != null && !Integer.valueOf(1).equals(createdCategory.getDeleted())) {
            productCategoryService.deleteCategory(createdCategoryId);
        }
    }

    @Test
    void adminShouldListSeedCategories() throws Exception {
        String token = login("admin", "Admin@123");

        MockHttpServletResponse response = mockMvc.perform(get("/api/admin/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        JsonNode json = objectMapper.readTree(response.getContentAsString());
        assertThat(json.path("code").asInt()).isEqualTo(0);
        assertThat(json.path("data").size()).isGreaterThanOrEqualTo(3);
        assertThat(json.path("data").get(0).path("categoryName").asText()).isEqualTo("手机数码");
    }

    @Test
    void customerShouldBeForbiddenFromCategoryManagement() throws Exception {
        String token = login("alice", "Admin@123");

        mockMvc.perform(get("/api/admin/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldCreateUpdateAndDeleteCategory() throws Exception {
        String token = login("admin", "Admin@123");
        String createBody = """
                {
                  "categoryName": "测试分类",
                  "parentId": 0,
                  "sort": 99,
                  "status": 1
                }
                """;

        MockHttpServletResponse createResponse = mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        JsonNode createdJson = objectMapper.readTree(createResponse.getContentAsString());
        createdCategoryId = createdJson.path("data").path("id").asLong();
        assertThat(createdCategoryId).isPositive();

        mockMvc.perform(put("/api/admin/categories/{id}", createdCategoryId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryName": "测试分类-已修改",
                                  "parentId": 0,
                                  "sort": 100,
                                  "status": 0
                                }
                                """))
                .andExpect(status().isOk());

        MockHttpServletResponse deleteResponse = mockMvc.perform(delete("/api/admin/categories/{id}", createdCategoryId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        JsonNode deletedJson = objectMapper.readTree(deleteResponse.getContentAsString());
        assertThat(deletedJson.path("code").asInt()).isEqualTo(0);
        assertThat(productCategoryService.getById(createdCategoryId).getDeleted()).isEqualTo(1);
        createdCategoryId = null;
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
