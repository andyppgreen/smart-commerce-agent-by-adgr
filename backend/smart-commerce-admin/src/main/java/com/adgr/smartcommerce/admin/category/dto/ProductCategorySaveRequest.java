package com.adgr.smartcommerce.admin.category.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductCategorySaveRequest(
        @NotBlank(message = "分类名称不能为空")
        @Size(max = 100, message = "分类名称不能超过100个字符")
        String categoryName,
        @PositiveOrZero(message = "父分类ID不能为负数")
        Long parentId,
        @PositiveOrZero(message = "排序值不能为负数")
        Integer sort,
        @Min(value = 0, message = "状态只能是0或1")
        @Max(value = 1, message = "状态只能是0或1")
        Integer status) {
}
