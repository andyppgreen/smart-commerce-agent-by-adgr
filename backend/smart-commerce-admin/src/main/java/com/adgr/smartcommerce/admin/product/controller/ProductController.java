package com.adgr.smartcommerce.admin.product.controller;

import com.adgr.smartcommerce.admin.auth.annotation.RequireRoles;
import com.adgr.smartcommerce.admin.common.response.ApiResponse;
import com.adgr.smartcommerce.admin.common.response.PageResponse;
import com.adgr.smartcommerce.admin.product.dto.ProductResponse;
import com.adgr.smartcommerce.admin.product.dto.ProductSaveRequest;
import com.adgr.smartcommerce.admin.product.entity.Product;
import com.adgr.smartcommerce.admin.product.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/products")
@RequireRoles("ADMIN")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> list(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") long current,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "每页数量必须大于0")
            @Max(value = 100, message = "每页最多查询100条") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @Positive(message = "分类ID必须大于0") Long categoryId,
            @RequestParam(required = false)
            @Min(value = 0, message = "商品状态只能是0或1")
            @Max(value = 1, message = "商品状态只能是0或1") Integer status) {
        return ApiResponse.success(PageResponse.from(
                productService.pageForAdmin(current, size, keyword, categoryId, status),
                ProductResponse::from));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(ProductResponse.from(productService.getActiveProduct(id)));
    }

    @PostMapping
    public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductSaveRequest request) {
        Product product = productService.createProduct(toEntity(request));
        return ApiResponse.success(ProductResponse.from(product));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductSaveRequest request) {
        Product product = productService.updateProduct(id, toEntity(request));
        return ApiResponse.success(ProductResponse.from(product));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.success(null);
    }

    private Product toEntity(ProductSaveRequest request) {
        Product product = new Product();
        product.setCategoryId(request.categoryId());
        product.setProductCode(request.productCode());
        product.setProductName(request.productName());
        product.setMainImage(request.mainImage());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setStatus(request.status());
        return product;
    }
}
