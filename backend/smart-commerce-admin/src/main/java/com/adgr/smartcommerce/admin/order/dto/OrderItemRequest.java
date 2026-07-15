package com.adgr.smartcommerce.admin.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotNull(message = "商品ID不能为空")
        @Positive(message = "商品ID必须大于0")
        Long productId,
        @NotNull(message = "购买数量不能为空")
        @Positive(message = "购买数量必须大于0")
        @Max(value = 999, message = "单个商品购买数量不能超过999")
        Integer quantity) {
}
