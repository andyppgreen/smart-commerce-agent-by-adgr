package com.adgr.smartcommerce.admin.product.service.impl;

import com.adgr.smartcommerce.admin.category.entity.ProductCategory;
import com.adgr.smartcommerce.admin.category.service.ProductCategoryService;
import com.adgr.smartcommerce.admin.common.exception.BusinessException;
import com.adgr.smartcommerce.admin.common.response.ResultCode;
import com.adgr.smartcommerce.admin.product.cache.ProductCatalogCache;
import com.adgr.smartcommerce.admin.product.cache.ProductCatalogCache.ProductCatalogCacheLookup;
import com.adgr.smartcommerce.admin.product.cache.HotProductCatalogCache;
import com.adgr.smartcommerce.admin.product.cache.HotProductCatalogCache.HotProductCatalogCacheLookup;
import com.adgr.smartcommerce.admin.product.dto.ProductCatalogResponse;
import com.adgr.smartcommerce.admin.product.entity.Product;
import com.adgr.smartcommerce.admin.product.mapper.ProductMapper;
import com.adgr.smartcommerce.admin.product.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductCategoryService productCategoryService;
    private final ProductCatalogCache productCatalogCache;
    private final HotProductCatalogCache hotProductCatalogCache;
    private final int hotProductLimit;

    public ProductServiceImpl(
            ProductCategoryService productCategoryService,
            ProductCatalogCache productCatalogCache,
            HotProductCatalogCache hotProductCatalogCache,
            @Value("${app.cache.hot-product-limit}") int hotProductLimit) {
        this.productCategoryService = productCategoryService;
        this.productCatalogCache = productCatalogCache;
        this.hotProductCatalogCache = hotProductCatalogCache;
        this.hotProductLimit = hotProductLimit;
    }

    @Override
    public IPage<Product> pageForAdmin(
            long current, long size, String keyword, Long categoryId, Integer status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getDeleted, 0)
                .eq(categoryId != null, Product::getCategoryId, categoryId)
                .eq(status != null, Product::getStatus, status)
                .and(StringUtils.hasText(keyword), query -> query
                        .like(Product::getProductName, keyword.trim())
                        .or()
                        .like(Product::getProductCode, keyword.trim()))
                .orderByDesc(Product::getId);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public IPage<Product> pagePublished(long current, long size, String keyword, Long categoryId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getDeleted, 0)
                .eq(Product::getStatus, 1)
                .eq(categoryId != null, Product::getCategoryId, categoryId)
                .and(StringUtils.hasText(keyword), query -> query
                        .like(Product::getProductName, keyword.trim()))
                .orderByDesc(Product::getId);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public Product getActiveProduct(Long id) {
        Product product = lambdaQuery()
                .eq(Product::getId, id)
                .eq(Product::getDeleted, 0)
                .one();
        if (product == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "商品不存在");
        }
        return product;
    }

    @Override
    public Product getPublishedProduct(Long id) {
        Product product = findPublishedProduct(id);
        if (product == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "商品不存在或已下架");
        }
        return product;
    }

    @Override
    public ProductCatalogResponse getPublishedProductCatalog(Long id) {
        ProductCatalogCacheLookup cacheLookup = productCatalogCache.getProductDetail(id);
        if (cacheLookup.hit()) {
            if (cacheLookup.empty()) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "商品不存在或已下架");
            }
            return cacheLookup.product();
        }

        Product product = findPublishedProduct(id);
        if (product == null) {
            productCatalogCache.putEmptyProductDetail(id);
            throw new BusinessException(ResultCode.BAD_REQUEST, "商品不存在或已下架");
        }

        ProductCatalogResponse response = ProductCatalogResponse.from(product);
        productCatalogCache.putProductDetail(id, response);
        return response;
    }

    @Override
    public List<ProductCatalogResponse> listHotProductCatalog() {
        HotProductCatalogCacheLookup cacheLookup = hotProductCatalogCache.getHotProducts();
        if (cacheLookup.hit()) {
            return cacheLookup.products();
        }

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getDeleted, 0)
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getSales)
                .orderByDesc(Product::getId);
        List<ProductCatalogResponse> products = page(
                        new Page<>(1, Math.max(1, hotProductLimit)), wrapper)
                .getRecords()
                .stream()
                .map(ProductCatalogResponse::from)
                .toList();
        hotProductCatalogCache.putHotProducts(products);
        return products;
    }

    @Override
    @Transactional
    public Product createProduct(Product product) {
        normalizeProductText(product);
        validateCategory(product.getCategoryId());
        validateProductCode(product.getProductCode(), null);

        product.setSales(0);
        product.setVersion(0);
        product.setDeleted(0);
        save(product);
        evictProductCaches(product.getId());
        return product;
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, Product product) {
        Product existing = getActiveProduct(id);
        normalizeProductText(product);
        validateCategory(product.getCategoryId());
        validateProductCode(product.getProductCode(), id);

        existing.setCategoryId(product.getCategoryId());
        existing.setProductCode(product.getProductCode());
        existing.setProductName(product.getProductName());
        existing.setMainImage(product.getMainImage());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        existing.setStatus(product.getStatus());
        if (!updateById(existing)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "商品已被其他操作更新，请刷新后重试");
        }
        evictProductCaches(id);
        return existing;
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        getActiveProduct(id);
        lambdaUpdate()
                .eq(Product::getId, id)
                .eq(Product::getDeleted, 0)
                .set(Product::getDeleted, 1)
                .update();
        evictProductCaches(id);
    }

    @Override
    public boolean deductStock(Long productId, Integer quantity) {
        boolean deducted = baseMapper.deductStock(productId, quantity) == 1;
        if (deducted) {
            evictProductCaches(productId);
        }
        return deducted;
    }

    private void evictProductCaches(Long productId) {
        productCatalogCache.evictProductDetail(productId);
        hotProductCatalogCache.evictHotProducts();
    }

    private Product findPublishedProduct(Long id) {
        return lambdaQuery()
                .eq(Product::getId, id)
                .eq(Product::getStatus, 1)
                .eq(Product::getDeleted, 0)
                .one();
    }

    private void validateCategory(Long categoryId) {
        ProductCategory category = productCategoryService.getById(categoryId);
        if (category == null || Integer.valueOf(1).equals(category.getDeleted())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "商品分类不存在");
        }
    }

    private void validateProductCode(String productCode, Long excludedId) {
        long count = lambdaQuery()
                .eq(Product::getProductCode, productCode)
                .ne(excludedId != null, Product::getId, excludedId)
                .count();
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "商品编码已存在");
        }
    }

    private void normalizeProductText(Product product) {
        product.setProductCode(product.getProductCode().trim());
        product.setProductName(product.getProductName().trim());
    }
}
