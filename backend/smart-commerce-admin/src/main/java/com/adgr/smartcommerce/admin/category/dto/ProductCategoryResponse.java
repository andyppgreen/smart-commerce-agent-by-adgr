package com.adgr.smartcommerce.admin.category.dto;

import com.adgr.smartcommerce.admin.category.entity.ProductCategory;
import java.time.LocalDateTime;

public record ProductCategoryResponse(
        Long id,
        Long parentId,
        String categoryName,
        Integer sort,
        Integer status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static ProductCategoryResponse from(ProductCategory category) {
        return new ProductCategoryResponse(
                category.getId(),
                category.getParentId(),
                category.getCategoryName(),
                category.getSort(),
                category.getStatus(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }
}
