package com.adgr.smartcommerce.admin.order.dto;

import com.adgr.smartcommerce.admin.order.entity.OrderItem;
import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        String productImage,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal totalPrice) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getProductImage(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getTotalPrice());
    }
}
