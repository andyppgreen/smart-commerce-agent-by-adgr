package com.adgr.smartcommerce.admin.product.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adgr.smartcommerce.admin.product.service.ProductService;
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
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductService productService;

    private Long createdProductId;

    @AfterEach
    void cleanUp() {
        if (createdProductId != null) {
            productService.removeById(createdProductId);
        }
    }

    @Test
    void adminShouldPageAndFilterProducts() throws Exception {
        String token = login("admin", "Admin@123");

        MockHttpServletResponse response = mockMvc.perform(get("/api/admin/products")
                        .header("Authorization", "Bearer " + token)
                        .param("current", "1")
                        .param("size", "10")
                        .param("keyword", "iPhone")
                        .param("categoryId", "1")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        JsonNode json = objectMapper.readTree(response.getContentAsString());
        assertThat(json.path("code").asInt()).isEqualTo(0);
        assertThat(json.path("data").path("total").asLong()).isGreaterThanOrEqualTo(1);
        assertThat(json.path("data").path("records").get(0).path("productName").asText())
                .contains("iPhone");
    }

    @Test
    void customerShouldBeForbiddenFromProductManagement() throws Exception {
        String token = login("alice", "Admin@123");

        mockMvc.perform(get("/api/admin/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldCreateUpdateAndDeleteProduct() throws Exception {
        String token = login("admin", "Admin@123");
        String productCode = "TEST-" + System.nanoTime();
        String createBody = """
                {
                  "categoryId": 1,
                  "productCode": "%s",
                  "productName": "测试商品",
                  "mainImage": "https://example.com/test-product.jpg",
                  "description": "商品管理集成测试数据",
                  "price": 19.90,
                  "stock": 20,
                  "status": 0
                }
                """.formatted(productCode);

        MockHttpServletResponse createResponse = mockMvc.perform(post("/api/admin/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        JsonNode createdJson = objectMapper.readTree(createResponse.getContentAsString());
        createdProductId = createdJson.path("data").path("id").asLong();
        assertThat(createdProductId).isPositive();
        assertThat(createdJson.path("data").path("sales").asInt()).isZero();

        mockMvc.perform(post("/api/admin/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/admin/products/{id}", createdProductId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": 1,
                                  "productCode": "%s",
                                  "productName": "测试商品-已修改",
                                  "mainImage": "https://example.com/test-product-updated.jpg",
                                  "description": "修改后的商品数据",
                                  "price": 29.90,
                                  "stock": 30,
                                  "status": 1
                                }
                                """.formatted(productCode)))
                .andExpect(status().isOk());

        MockHttpServletResponse detailResponse = mockMvc.perform(get("/api/admin/products/{id}", createdProductId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        JsonNode detailJson = objectMapper.readTree(detailResponse.getContentAsString());
        assertThat(detailJson.path("data").path("productName").asText()).isEqualTo("测试商品-已修改");
        assertThat(detailJson.path("data").path("stock").asInt()).isEqualTo(30);
        assertThat(detailJson.path("data").path("status").asInt()).isEqualTo(1);

        mockMvc.perform(delete("/api/admin/products/{id}", createdProductId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        assertThat(productService.getById(createdProductId).getDeleted()).isEqualTo(1);
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
