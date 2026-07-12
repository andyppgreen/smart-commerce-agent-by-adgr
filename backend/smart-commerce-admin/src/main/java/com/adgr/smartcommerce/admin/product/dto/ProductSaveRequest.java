package com.adgr.smartcommerce.admin.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductSaveRequest(
        @NotNull(message = "商品分类不能为空")
        @Positive(message = "商品分类ID必须大于0")
        Long categoryId,
        @NotBlank(message = "商品编码不能为空")
        @Size(max = 64, message = "商品编码不能超过64个字符")
        String productCode,
        @NotBlank(message = "商品名称不能为空")
        @Size(max = 200, message = "商品名称不能超过200个字符")
        String productName,
        @Size(max = 500, message = "商品主图地址不能超过500个字符")
        String mainImage,
        @Size(max = 65535, message = "商品描述过长")
        String description,
        @NotNull(message = "商品价格不能为空")
        @DecimalMin(value = "0.00", message = "商品价格不能为负数")
        @Digits(integer = 8, fraction = 2, message = "商品价格最多8位整数和2位小数")
        BigDecimal price,
        @NotNull(message = "商品库存不能为空")
        @PositiveOrZero(message = "商品库存不能为负数")
        Integer stock,
        @NotNull(message = "商品状态不能为空")
        @Min(value = 0, message = "商品状态只能是0或1")
        @Max(value = 1, message = "商品状态只能是0或1")
        Integer status) {
}
