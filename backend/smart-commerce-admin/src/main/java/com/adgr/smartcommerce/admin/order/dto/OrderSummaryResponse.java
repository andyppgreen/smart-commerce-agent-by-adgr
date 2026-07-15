package com.adgr.smartcommerce.admin.order.dto;

import com.adgr.smartcommerce.admin.order.entity.OrderInfo;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResponse(
        Long id,
        String orderNo,
        Long userId,
        BigDecimal totalAmount,
        BigDecimal payAmount,
        Integer orderStatus,
        Integer payStatus,
        Integer sourceType,
        LocalDateTime createdAt) {

    public static OrderSummaryResponse from(OrderInfo order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderNo(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getPayAmount(),
                order.getOrderStatus(),
                order.getPayStatus(),
                order.getSourceType(),
                order.getCreatedAt());
    }
}
