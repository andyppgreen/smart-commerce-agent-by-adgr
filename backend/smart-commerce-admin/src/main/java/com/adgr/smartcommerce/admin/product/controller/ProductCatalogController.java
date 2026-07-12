package com.adgr.smartcommerce.admin.product.controller;

import com.adgr.smartcommerce.admin.common.response.ApiResponse;
import com.adgr.smartcommerce.admin.common.response.PageResponse;
import com.adgr.smartcommerce.admin.product.dto.ProductCatalogResponse;
import com.adgr.smartcommerce.admin.product.service.ProductService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/products")
public class ProductCatalogController {

    private final ProductService productService;

    public ProductCatalogController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ProductCatalogResponse>> list(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") long current,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "每页数量必须大于0")
            @Max(value = 100, message = "每页最多查询100条") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @Positive(message = "分类ID必须大于0") Long categoryId) {
        return ApiResponse.success(PageResponse.from(
                productService.pagePublished(current, size, keyword, categoryId),
                ProductCatalogResponse::from));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductCatalogResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(ProductCatalogResponse.from(productService.getPublishedProduct(id)));
    }
}
