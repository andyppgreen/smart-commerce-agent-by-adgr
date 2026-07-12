package com.adgr.smartcommerce.admin.category.service;

import com.adgr.smartcommerce.admin.category.entity.ProductCategory;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface ProductCategoryService extends IService<ProductCategory> {

    List<ProductCategory> listForAdmin();

    ProductCategory createCategory(ProductCategory category);

    ProductCategory updateCategory(Long id, ProductCategory category);

    void deleteCategory(Long id);
}
