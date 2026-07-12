package com.adgr.smartcommerce.admin.product.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adgr.smartcommerce.admin.product.entity.Product;
import com.adgr.smartcommerce.admin.product.service.ProductService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class ProductCatalogControllerIntegrationTest {

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
    void anonymousUserShouldPagePublishedProducts() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/api/products")
                        .param("current", "1")
                        .param("size", "10")
                        .param("keyword", "iPhone")
                        .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        JsonNode json = objectMapper.readTree(response.getContentAsString());
        JsonNode firstProduct = json.path("data").path("records").get(0);
        assertThat(json.path("data").path("total").asLong()).isGreaterThanOrEqualTo(1);
        assertThat(firstProduct.path("productName").asText()).contains("iPhone");
        assertThat(firstProduct.has("stock")).isFalse();
        assertThat(firstProduct.has("version")).isFalse();
        assertThat(firstProduct.path("available").asBoolean()).isTrue();
    }

    @Test
    void anonymousUserShouldGetPublishedProductDetail() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        JsonNode json = objectMapper.readTree(response.getContentAsString());
        assertThat(json.path("data").path("id").asLong()).isEqualTo(1L);
        assertThat(json.path("data").path("productName").asText()).contains("iPhone");
    }

    @Test
    void anonymousUserShouldNotSeeOfflineProduct() throws Exception {
        Product product = new Product();
        product.setCategoryId(1L);
        product.setProductCode("OFFLINE-" + System.nanoTime());
        product.setProductName("下架测试商品");
        product.setPrice(new BigDecimal("9.90"));
        product.setStock(10);
        product.setStatus(0);
        createdProductId = productService.createProduct(product).getId();

        mockMvc.perform(get("/api/products/{id}", createdProductId))
                .andExpect(status().isBadRequest());
    }
}
