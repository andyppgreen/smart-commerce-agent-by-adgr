package com.adgr.smartcommerce.admin.product.dto;

import com.adgr.smartcommerce.admin.product.entity.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        Long categoryId,
        String productCode,
        String productName,
        String mainImage,
        String description,
        BigDecimal price,
        Integer stock,
        Integer sales,
        Integer status,
        Integer version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCategoryId(),
                product.getProductCode(),
                product.getProductName(),
                product.getMainImage(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getSales(),
                product.getStatus(),
                product.getVersion(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
