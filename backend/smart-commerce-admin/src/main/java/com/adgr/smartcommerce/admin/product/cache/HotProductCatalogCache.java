package com.adgr.smartcommerce.admin.product.cache;

import com.adgr.smartcommerce.admin.product.dto.ProductCatalogResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class HotProductCatalogCache {

    private static final Logger log = LoggerFactory.getLogger(HotProductCatalogCache.class);
    private static final String HOT_PRODUCT_KEY = "product:catalog:hot";
    private static final TypeReference<List<ProductCatalogResponse>> PRODUCT_LIST_TYPE =
            new TypeReference<>() {};

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration hotProductTtl;

    public HotProductCatalogCache(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${app.cache.hot-product-ttl-seconds}") long hotProductTtlSeconds) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.hotProductTtl = Duration.ofSeconds(hotProductTtlSeconds);
    }

    public HotProductCatalogCacheLookup getHotProducts() {
        try {
            String value = redisTemplate.opsForValue().get(HOT_PRODUCT_KEY);
            if (value == null) {
                return HotProductCatalogCacheLookup.miss();
            }
            return HotProductCatalogCacheLookup.hit(
                    objectMapper.readValue(value, PRODUCT_LIST_TYPE));
        } catch (JsonProcessingException ex) {
            log.warn("Failed to deserialize hot product cache, reason={}", ex.getMessage());
            evictHotProducts();
            return HotProductCatalogCacheLookup.miss();
        } catch (RuntimeException ex) {
            log.warn("Failed to read hot product cache, reason={}: {}",
                    ex.getClass().getSimpleName(), ex.getMessage());
            return HotProductCatalogCacheLookup.miss();
        }
    }

    public void putHotProducts(List<ProductCatalogResponse> products) {
        try {
            redisTemplate.opsForValue().set(
                    HOT_PRODUCT_KEY,
                    objectMapper.writeValueAsString(products),
                    hotProductTtl);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to serialize hot product cache, reason={}", ex.getMessage());
        } catch (RuntimeException ex) {
            log.warn("Failed to write hot product cache, reason={}: {}",
                    ex.getClass().getSimpleName(), ex.getMessage());
        }
    }

    public void evictHotProducts() {
        try {
            redisTemplate.delete(HOT_PRODUCT_KEY);
        } catch (RuntimeException ex) {
            log.warn("Failed to evict hot product cache, reason={}: {}",
                    ex.getClass().getSimpleName(), ex.getMessage());
        }
    }

    public record HotProductCatalogCacheLookup(
            boolean hit,
            List<ProductCatalogResponse> products) {

        public static HotProductCatalogCacheLookup miss() {
            return new HotProductCatalogCacheLookup(false, List.of());
        }

        public static HotProductCatalogCacheLookup hit(List<ProductCatalogResponse> products) {
            return new HotProductCatalogCacheLookup(true, products);
        }
    }
}
