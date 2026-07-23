package com.adgr.smartcommerce.admin.product.cache;

import com.adgr.smartcommerce.admin.product.dto.ProductCatalogResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductCatalogCache {

    private static final Logger log = LoggerFactory.getLogger(ProductCatalogCache.class);
    private static final String PRODUCT_DETAIL_KEY_PREFIX = "product:catalog:detail:";
    private static final String EMPTY_VALUE = "__NULL__";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration productDetailTtl;
    private final Duration productNullTtl;

    public ProductCatalogCache(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${app.cache.product-detail-ttl-seconds}") long productDetailTtlSeconds,
            @Value("${app.cache.product-null-ttl-seconds}") long productNullTtlSeconds) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.productDetailTtl = Duration.ofSeconds(productDetailTtlSeconds);
        this.productNullTtl = Duration.ofSeconds(productNullTtlSeconds);
    }

    public ProductCatalogCacheLookup getProductDetail(Long productId) {
        String key = productDetailKey(productId);
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return ProductCatalogCacheLookup.miss();
            }
            if (EMPTY_VALUE.equals(value)) {
                return ProductCatalogCacheLookup.emptyHit();
            }
            ProductCatalogResponse product = objectMapper.readValue(value, ProductCatalogResponse.class);
            return ProductCatalogCacheLookup.hit(product);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to deserialize product catalog cache, key={}, reason={}", key, ex.getMessage());
            evictProductDetail(productId);
            return ProductCatalogCacheLookup.miss();
        } catch (RuntimeException ex) {
            log.warn("Failed to read product catalog cache, key={}, reason={}: {}",
                    key, ex.getClass().getSimpleName(), ex.getMessage());
            return ProductCatalogCacheLookup.miss();
        }
    }

    public void putProductDetail(Long productId, ProductCatalogResponse product) {
        String key = productDetailKey(productId);
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(product), productDetailTtl);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to serialize product catalog cache, key={}, reason={}", key, ex.getMessage());
        } catch (RuntimeException ex) {
            log.warn("Failed to write product catalog cache, key={}, reason={}: {}",
                    key, ex.getClass().getSimpleName(), ex.getMessage());
        }
    }

    public void putEmptyProductDetail(Long productId) {
        String key = productDetailKey(productId);
        try {
            redisTemplate.opsForValue().set(key, EMPTY_VALUE, productNullTtl);
        } catch (RuntimeException ex) {
            log.warn("Failed to write empty product catalog cache, key={}, reason={}: {}",
                    key, ex.getClass().getSimpleName(), ex.getMessage());
        }
    }

    public void evictProductDetail(Long productId) {
        String key = productDetailKey(productId);
        try {
            redisTemplate.delete(key);
        } catch (RuntimeException ex) {
            log.warn("Failed to evict product catalog cache, key={}, reason={}: {}",
                    key, ex.getClass().getSimpleName(), ex.getMessage());
        }
    }

    private String productDetailKey(Long productId) {
        return PRODUCT_DETAIL_KEY_PREFIX + productId;
    }

    public record ProductCatalogCacheLookup(
            boolean hit,
            boolean empty,
            ProductCatalogResponse product) {

        public static ProductCatalogCacheLookup miss() {
            return new ProductCatalogCacheLookup(false, false, null);
        }

        public static ProductCatalogCacheLookup emptyHit() {
            return new ProductCatalogCacheLookup(true, true, null);
        }

        public static ProductCatalogCacheLookup hit(ProductCatalogResponse product) {
            return new ProductCatalogCacheLookup(true, false, product);
        }
    }
}
