package com.adgr.smartcommerce.admin.product.dto;

import com.adgr.smartcommerce.admin.product.entity.Product;
import java.math.BigDecimal;

public record ProductCatalogResponse(
        Long id,
        Long categoryId,
        String productName,
        String mainImage,
        String description,
        BigDecimal price,
        Integer sales,
        boolean available) {

    public static ProductCatalogResponse from(Product product) {
        return new ProductCatalogResponse(
                product.getId(),
                product.getCategoryId(),
                product.getProductName(),
                product.getMainImage(),
                product.getDescription(),
                product.getPrice(),
                product.getSales(),
                product.getStock() != null && product.getStock() > 0);
    }
}
