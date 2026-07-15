package com.adgr.smartcommerce.admin.order.dto;

import com.adgr.smartcommerce.admin.order.entity.OrderInfo;
import com.adgr.smartcommerce.admin.order.entity.OrderItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long id,
        String orderNo,
        Long userId,
        BigDecimal totalAmount,
        BigDecimal payAmount,
        Integer orderStatus,
        Integer payStatus,
        Integer sourceType,
        String receiverName,
        String receiverPhone,
        String receiverAddress,
        LocalDateTime payTime,
        LocalDateTime cancelTime,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderItemResponse> items) {

    public static OrderDetailResponse from(OrderInfo order, List<OrderItem> items) {
        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNo(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getPayAmount(),
                order.getOrderStatus(),
                order.getPayStatus(),
                order.getSourceType(),
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getReceiverAddress(),
                order.getPayTime(),
                order.getCancelTime(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items.stream().map(OrderItemResponse::from).toList());
    }
}
