package com.adgr.smartcommerce.admin.order.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adgr.smartcommerce.admin.order.entity.OrderInfo;
import com.adgr.smartcommerce.admin.order.entity.OrderItem;
import com.adgr.smartcommerce.admin.order.service.OrderItemService;
import com.adgr.smartcommerce.admin.order.service.OrderService;
import com.adgr.smartcommerce.admin.product.entity.Product;
import com.adgr.smartcommerce.admin.product.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ProductService productService;

    private final Map<Long, ProductSnapshot> productSnapshots = new HashMap<>();
    private String receiverMarker;

    @AfterEach
    void cleanUp() {
        if (receiverMarker != null) {
            List<OrderInfo> testOrders = orderService.lambdaQuery()
                    .eq(OrderInfo::getReceiverName, receiverMarker)
                    .list();
            for (OrderInfo order : testOrders) {
                orderItemService.remove(new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, order.getId()));
                orderService.removeById(order.getId());
            }
        }
        productSnapshots.forEach((productId, snapshot) -> productService.lambdaUpdate()
                .eq(Product::getId, productId)
                .set(Product::getStock, snapshot.stock())
                .set(Product::getVersion, snapshot.version())
                .update());
    }

    @Test
    void customerShouldCreateAndQueryMultiProductOrder() throws Exception {
        snapshotProduct(2L);
        snapshotProduct(3L);
        receiverMarker = "测试收货人-" + System.nanoTime();
        String customerToken = login("alice", "Admin@123");

        MockHttpServletResponse createResponse = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {"productId": 2, "quantity": 1},
                                    {"productId": 3, "quantity": 2}
                                  ],
                                  "receiverName": "%s",
                                  "receiverPhone": "18800009999",
                                  "receiverAddress": "上海市测试路 1 号"
                                }
                                """.formatted(receiverMarker)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        JsonNode createdJson = objectMapper.readTree(createResponse.getContentAsString()).path("data");
        long orderId = createdJson.path("id").asLong();
        assertThat(orderId).isPositive();
        assertThat(createdJson.path("userId").asLong()).isEqualTo(2L);
        assertThat(createdJson.path("totalAmount").decimalValue())
                .isEqualByComparingTo(new BigDecimal("1097.00"));
        assertThat(createdJson.path("orderStatus").asInt()).isZero();
        assertThat(createdJson.path("payStatus").asInt()).isZero();
        assertThat(createdJson.path("items").size()).isEqualTo(2);

        MockHttpServletResponse listResponse = mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .param("orderStatus", "0"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        JsonNode listJson = objectMapper.readTree(listResponse.getContentAsString());
        assertThat(listJson.path("data").path("records").toString())
                .contains(createdJson.path("orderNo").asText());

        mockMvc.perform(get("/api/orders/{id}", orderId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk());

        String anotherCustomerToken = login("bob", "Admin@123");
        mockMvc.perform(get("/api/orders/{id}", orderId)
                        .header("Authorization", "Bearer " + anotherCustomerToken))
                .andExpect(status().isBadRequest());

        String adminToken = login("admin", "Admin@123");
        mockMvc.perform(get("/api/admin/orders/{id}", orderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        assertThat(productService.getById(2L).getStock())
                .isEqualTo(productSnapshots.get(2L).stock() - 1);
        assertThat(productService.getById(3L).getStock())
                .isEqualTo(productSnapshots.get(3L).stock() - 2);
    }

    @Test
    void orderCreationShouldRejectInsufficientStock() throws Exception {
        snapshotProduct(1L);
        receiverMarker = "库存测试-" + System.nanoTime();
        String token = login("alice", "Admin@123");

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [{"productId": 1, "quantity": 999}],
                                  "receiverName": "%s",
                                  "receiverPhone": "18800009999",
                                  "receiverAddress": "上海市测试路 2 号"
                                }
                                """.formatted(receiverMarker)))
                .andExpect(status().isBadRequest());

        assertThat(productService.getById(1L).getStock())
                .isEqualTo(productSnapshots.get(1L).stock());
        assertThat(orderService.lambdaQuery()
                .eq(OrderInfo::getReceiverName, receiverMarker)
                .count()).isZero();
    }

    @Test
    void orderEndpointsShouldEnforceCustomerAndAdminRoles() throws Exception {
        String customerToken = login("alice", "Admin@123");
        String adminToken = login("admin", "Admin@123");

        mockMvc.perform(get("/api/admin/orders")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    private void snapshotProduct(Long productId) {
        Product product = productService.getById(productId);
        productSnapshots.put(productId, new ProductSnapshot(product.getStock(), product.getVersion()));
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

    private record ProductSnapshot(Integer stock, Integer version) {
    }
}
