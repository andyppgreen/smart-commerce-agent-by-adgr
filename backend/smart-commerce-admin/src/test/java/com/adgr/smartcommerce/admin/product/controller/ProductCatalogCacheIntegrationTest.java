package com.adgr.smartcommerce.admin.product.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adgr.smartcommerce.admin.product.cache.ProductCatalogCache;
import com.adgr.smartcommerce.admin.product.cache.ProductCatalogCache.ProductCatalogCacheLookup;
import com.adgr.smartcommerce.admin.product.cache.HotProductCatalogCache;
import com.adgr.smartcommerce.admin.product.dto.ProductCatalogResponse;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class ProductCatalogCacheIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductService productService;

    @MockBean
    private ProductCatalogCache productCatalogCache;

    @MockBean
    private HotProductCatalogCache hotProductCatalogCache;

    private Long createdProductId;

    @AfterEach
    void cleanUp() {
        if (createdProductId != null) {
            productService.removeById(createdProductId);
        }
    }

    @Test
    void productDetailShouldReturnCachedValueWhenCacheHit() throws Exception {
        when(productCatalogCache.getProductDetail(999L)).thenReturn(ProductCatalogCacheLookup.hit(
                new ProductCatalogResponse(
                        999L,
                        1L,
                        "缓存命中商品",
                        "https://example.com/cache-hit.jpg",
                        "来自缓存",
                        new BigDecimal("9.90"),
                        8,
                        true)));

        MockHttpServletResponse response = mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        JsonNode json = objectMapper.readTree(response.getContentAsString());
        assertThat(json.path("data").path("productName").asText()).isEqualTo("缓存命中商品");
        verify(productCatalogCache, never()).putProductDetail(eq(999L), any());
        verify(productCatalogCache, never()).putEmptyProductDetail(999L);
    }

    @Test
    void productDetailShouldWriteCacheAfterCacheMiss() throws Exception {
        when(productCatalogCache.getProductDetail(1L)).thenReturn(ProductCatalogCacheLookup.miss());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk());

        verify(productCatalogCache).putProductDetail(
                eq(1L),
                argThat(product -> product.id().equals(1L)
                        && product.productName().contains("iPhone")
                        && product.available()));
    }

    @Test
    void productDetailShouldKeepBusinessErrorWhenEmptyCacheHit() throws Exception {
        when(productCatalogCache.getProductDetail(999L)).thenReturn(ProductCatalogCacheLookup.emptyHit());

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isBadRequest());

        verify(productCatalogCache, never()).putEmptyProductDetail(999L);
    }

    @Test
    void productCreateShouldEvictProductDetailCache() {
        Product product = createProduct("CACHE-CREATE-" + System.nanoTime(), "创建缓存商品", 1, 10);
        createdProductId = product.getId();

        verify(productCatalogCache).evictProductDetail(createdProductId);
        verify(hotProductCatalogCache).evictHotProducts();
    }

    @Test
    void productUpdateAndDeleteShouldEvictProductDetailCache() {
        Product product = createProduct("CACHE-EVICT-" + System.nanoTime(), "缓存失效商品", 1, 10);
        createdProductId = product.getId();
        clearInvocations(productCatalogCache, hotProductCatalogCache);

        Product update = new Product();
        update.setCategoryId(1L);
        update.setProductCode(product.getProductCode());
        update.setProductName("缓存失效商品-已修改");
        update.setMainImage("https://example.com/cache-evict-updated.jpg");
        update.setDescription("修改后详情");
        update.setPrice(new BigDecimal("29.90"));
        update.setStock(20);
        update.setStatus(1);

        productService.updateProduct(createdProductId, update);
        productService.deleteProduct(createdProductId);

        verify(productCatalogCache, times(2)).evictProductDetail(createdProductId);
        verify(hotProductCatalogCache, times(2)).evictHotProducts();
    }

    @Test
    void stockDeductionShouldEvictProductDetailCache() {
        Product product = createProduct("CACHE-STOCK-" + System.nanoTime(), "库存缓存商品", 1, 10);
        createdProductId = product.getId();
        clearInvocations(productCatalogCache, hotProductCatalogCache);

        boolean deducted = productService.deductStock(createdProductId, 1);

        assertThat(deducted).isTrue();
        verify(productCatalogCache).evictProductDetail(createdProductId);
        verify(hotProductCatalogCache).evictHotProducts();
    }

    private Product createProduct(String productCode, String productName, int status, int stock) {
        Product product = new Product();
        product.setCategoryId(1L);
        product.setProductCode(productCode);
        product.setProductName(productName);
        product.setMainImage("https://example.com/cache-product.jpg");
        product.setDescription("缓存测试商品");
        product.setPrice(new BigDecimal("19.90"));
        product.setStock(stock);
        product.setStatus(status);
        return productService.createProduct(product);
    }
}
