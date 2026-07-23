package com.adgr.smartcommerce.admin.product.service;

import com.adgr.smartcommerce.admin.product.entity.Product;
import com.adgr.smartcommerce.admin.product.dto.ProductCatalogResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface ProductService extends IService<Product> {

    IPage<Product> pageForAdmin(long current, long size, String keyword, Long categoryId, Integer status);

    IPage<Product> pagePublished(long current, long size, String keyword, Long categoryId);

    Product getActiveProduct(Long id);

    Product getPublishedProduct(Long id);

    ProductCatalogResponse getPublishedProductCatalog(Long id);

    List<ProductCatalogResponse> listHotProductCatalog();

    Product createProduct(Product product);

    Product updateProduct(Long id, Product product);

    void deleteProduct(Long id);

    boolean deductStock(Long productId, Integer quantity);
}
