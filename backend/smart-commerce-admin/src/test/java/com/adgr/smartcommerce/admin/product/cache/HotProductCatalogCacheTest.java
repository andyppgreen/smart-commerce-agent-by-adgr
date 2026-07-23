package com.adgr.smartcommerce.admin.product.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.adgr.smartcommerce.admin.product.cache.HotProductCatalogCache.HotProductCatalogCacheLookup;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class HotProductCatalogCacheTest {

    private static final String HOT_PRODUCT_KEY = "product:catalog:hot";

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private HotProductCatalogCache hotProductCatalogCache;

    @BeforeEach
    void setUp() {
        hotProductCatalogCache = new HotProductCatalogCache(redisTemplate, new ObjectMapper(), 300);
    }

    @Test
    void getHotProductsShouldReturnCachedList() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(HOT_PRODUCT_KEY)).thenReturn("""
                [{"id":3,"categoryId":3,"productName":"热门商品","mainImage":"image",
                "description":"description","price":99.00,"sales":80,"available":true}]
                """);

        HotProductCatalogCacheLookup lookup = hotProductCatalogCache.getHotProducts();

        assertThat(lookup.hit()).isTrue();
        assertThat(lookup.products()).hasSize(1);
        assertThat(lookup.products().get(0).productName()).isEqualTo("热门商品");
    }

    @Test
    void getHotProductsShouldDegradeWhenRedisFails() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(HOT_PRODUCT_KEY))
                .thenThrow(new RedisConnectionFailureException("down"));

        HotProductCatalogCacheLookup lookup = hotProductCatalogCache.getHotProducts();

        assertThat(lookup.hit()).isFalse();
    }

    @Test
    void putHotProductsShouldWriteListWithTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        List<com.adgr.smartcommerce.admin.product.dto.ProductCatalogResponse> products = List.of(
                new com.adgr.smartcommerce.admin.product.dto.ProductCatalogResponse(
                        3L, 3L, "热门商品", "image", "description", new BigDecimal("99.00"), 80, true));

        hotProductCatalogCache.putHotProducts(products);

        verify(valueOperations).set(
                eq(HOT_PRODUCT_KEY),
                eq("[{\"id\":3,\"categoryId\":3,\"productName\":\"热门商品\","
                        + "\"mainImage\":\"image\",\"description\":\"description\","
                        + "\"price\":99.00,\"sales\":80,\"available\":true}]"),
                eq(Duration.ofSeconds(300)));
    }
}
