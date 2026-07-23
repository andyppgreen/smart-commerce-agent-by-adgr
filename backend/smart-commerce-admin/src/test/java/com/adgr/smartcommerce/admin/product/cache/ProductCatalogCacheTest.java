package com.adgr.smartcommerce.admin.product.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.adgr.smartcommerce.admin.product.cache.ProductCatalogCache.ProductCatalogCacheLookup;
import com.adgr.smartcommerce.admin.product.dto.ProductCatalogResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class ProductCatalogCacheTest {

    private static final String PRODUCT_DETAIL_KEY = "product:catalog:detail:1";

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private ProductCatalogCache productCatalogCache;

    @BeforeEach
    void setUp() {
        productCatalogCache = new ProductCatalogCache(redisTemplate, new ObjectMapper(), 1800, 60);
    }

    @Test
    void getProductDetailShouldReturnHitWhenValueExists() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(PRODUCT_DETAIL_KEY)).thenReturn("""
                {
                  "id": 1,
                  "categoryId": 1,
                  "productName": "缓存商品",
                  "mainImage": "https://example.com/product.jpg",
                  "description": "缓存详情",
                  "price": 19.90,
                  "sales": 3,
                  "available": true
                }
                """);

        ProductCatalogCacheLookup lookup = productCatalogCache.getProductDetail(1L);

        assertThat(lookup.hit()).isTrue();
        assertThat(lookup.empty()).isFalse();
        assertThat(lookup.product().productName()).isEqualTo("缓存商品");
        assertThat(lookup.product().available()).isTrue();
    }

    @Test
    void getProductDetailShouldReturnEmptyHitWhenEmptyValueExists() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(PRODUCT_DETAIL_KEY)).thenReturn("__NULL__");

        ProductCatalogCacheLookup lookup = productCatalogCache.getProductDetail(1L);

        assertThat(lookup.hit()).isTrue();
        assertThat(lookup.empty()).isTrue();
        assertThat(lookup.product()).isNull();
    }

    @Test
    void getProductDetailShouldReturnMissWhenRedisFails() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(PRODUCT_DETAIL_KEY)).thenThrow(new RedisConnectionFailureException("down"));

        ProductCatalogCacheLookup lookup = productCatalogCache.getProductDetail(1L);

        assertThat(lookup.hit()).isFalse();
    }

    @Test
    void putProductDetailShouldWriteSerializedValueWithTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        ProductCatalogResponse response = new ProductCatalogResponse(
                1L,
                1L,
                "缓存商品",
                "https://example.com/product.jpg",
                "缓存详情",
                new BigDecimal("19.90"),
                3,
                true);

        productCatalogCache.putProductDetail(1L, response);

        verify(valueOperations).set(
                eq(PRODUCT_DETAIL_KEY),
                eq("{\"id\":1,\"categoryId\":1,\"productName\":\"缓存商品\","
                        + "\"mainImage\":\"https://example.com/product.jpg\","
                        + "\"description\":\"缓存详情\",\"price\":19.90,\"sales\":3,\"available\":true}"),
                eq(Duration.ofSeconds(1800)));
    }

    @Test
    void putEmptyProductDetailShouldWriteEmptyValueWithShortTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        productCatalogCache.putEmptyProductDetail(1L);

        verify(valueOperations).set(PRODUCT_DETAIL_KEY, "__NULL__", Duration.ofSeconds(60));
    }

    @Test
    void evictProductDetailShouldDeleteKey() {
        productCatalogCache.evictProductDetail(1L);

        verify(redisTemplate).delete(PRODUCT_DETAIL_KEY);
    }
}
