package com.adgr.smartcommerce.admin.product.service;

import com.adgr.smartcommerce.admin.product.entity.Product;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ProductService extends IService<Product> {

    IPage<Product> pageForAdmin(long current, long size, String keyword, Long categoryId, Integer status);

    IPage<Product> pagePublished(long current, long size, String keyword, Long categoryId);

    Product getActiveProduct(Long id);

    Product getPublishedProduct(Long id);

    Product createProduct(Product product);

    Product updateProduct(Long id, Product product);

    void deleteProduct(Long id);
}
