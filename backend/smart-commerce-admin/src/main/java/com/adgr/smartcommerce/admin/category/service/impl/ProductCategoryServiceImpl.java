package com.adgr.smartcommerce.admin.category.service.impl;

import com.adgr.smartcommerce.admin.category.entity.ProductCategory;
import com.adgr.smartcommerce.admin.category.mapper.ProductCategoryMapper;
import com.adgr.smartcommerce.admin.category.service.ProductCategoryService;
import com.adgr.smartcommerce.admin.common.exception.BusinessException;
import com.adgr.smartcommerce.admin.common.response.ResultCode;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory>
        implements ProductCategoryService {

    @Override
    public List<ProductCategory> listForAdmin() {
        return lambdaQuery()
                .eq(ProductCategory::getDeleted, 0)
                .orderByAsc(ProductCategory::getParentId)
                .orderByAsc(ProductCategory::getSort)
                .orderByAsc(ProductCategory::getId)
                .list();
    }

    @Override
    @Transactional
    public ProductCategory createCategory(ProductCategory category) {
        Long parentId = normalizeParentId(category.getParentId());
        validateParent(null, parentId);

        category.setParentId(parentId);
        category.setSort(normalizeSort(category.getSort()));
        category.setStatus(normalizeStatus(category.getStatus()));
        category.setDeleted(0);
        save(category);
        return category;
    }

    @Override
    @Transactional
    public ProductCategory updateCategory(Long id, ProductCategory category) {
        ProductCategory existing = requireActiveCategory(id);
        Long parentId = category.getParentId() == null
                ? normalizeParentId(existing.getParentId())
                : category.getParentId();
        validateParent(id, parentId);

        existing.setParentId(parentId);
        existing.setCategoryName(category.getCategoryName());
        existing.setSort(category.getSort() == null
                ? normalizeSort(existing.getSort())
                : category.getSort());
        existing.setStatus(category.getStatus() == null
                ? normalizeStatus(existing.getStatus())
                : category.getStatus());
        updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        requireActiveCategory(id);
        long childCount = lambdaQuery()
                .eq(ProductCategory::getParentId, id)
                .eq(ProductCategory::getDeleted, 0)
                .count();
        if (childCount > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请先处理该分类下的子分类");
        }
        lambdaUpdate()
                .eq(ProductCategory::getId, id)
                .eq(ProductCategory::getDeleted, 0)
                .set(ProductCategory::getDeleted, 1)
                .update();
    }

    private ProductCategory requireActiveCategory(Long id) {
        ProductCategory category = lambdaQuery()
                .eq(ProductCategory::getId, id)
                .eq(ProductCategory::getDeleted, 0)
                .one();
        if (category == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "商品分类不存在");
        }
        return category;
    }

    private void validateParent(Long categoryId, Long parentId) {
        if (parentId == 0) {
            return;
        }
        if (categoryId != null && categoryId.equals(parentId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "分类不能设置自己为父分类");
        }

        ProductCategory parent = requireActiveCategory(parentId);
        if (categoryId == null) {
            return;
        }

        Set<Long> visited = new HashSet<>();
        Long currentId = parent.getId();
        while (currentId != null && currentId != 0 && visited.add(currentId)) {
            if (categoryId.equals(currentId)) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "不能将分类移动到自己的子分类下");
            }
            ProductCategory current = requireActiveCategory(currentId);
            currentId = normalizeParentId(current.getParentId());
        }
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }

    private Integer normalizeSort(Integer sort) {
        return sort == null ? 0 : sort;
    }

    private Integer normalizeStatus(Integer status) {
        return status == null ? 1 : status;
    }
}
