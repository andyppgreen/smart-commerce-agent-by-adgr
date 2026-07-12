package com.adgr.smartcommerce.admin.category.controller;

import com.adgr.smartcommerce.admin.auth.annotation.RequireRoles;
import com.adgr.smartcommerce.admin.category.dto.ProductCategoryResponse;
import com.adgr.smartcommerce.admin.category.dto.ProductCategorySaveRequest;
import com.adgr.smartcommerce.admin.category.entity.ProductCategory;
import com.adgr.smartcommerce.admin.category.service.ProductCategoryService;
import com.adgr.smartcommerce.admin.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/categories")
@RequireRoles("ADMIN")
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    public ProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    @GetMapping
    public ApiResponse<List<ProductCategoryResponse>> list() {
        return ApiResponse.success(productCategoryService.listForAdmin()
                .stream()
                .map(ProductCategoryResponse::from)
                .toList());
    }

    @PostMapping
    public ApiResponse<ProductCategoryResponse> create(
            @Valid @RequestBody ProductCategorySaveRequest request) {
        ProductCategory category = productCategoryService.createCategory(toEntity(request));
        return ApiResponse.success(ProductCategoryResponse.from(category));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductCategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductCategorySaveRequest request) {
        ProductCategory category = productCategoryService.updateCategory(id, toEntity(request));
        return ApiResponse.success(ProductCategoryResponse.from(category));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productCategoryService.deleteCategory(id);
        return ApiResponse.success(null);
    }

    private ProductCategory toEntity(ProductCategorySaveRequest request) {
        ProductCategory category = new ProductCategory();
        category.setCategoryName(request.categoryName());
        category.setParentId(request.parentId());
        category.setSort(request.sort());
        category.setStatus(request.status());
        return category;
    }
}
