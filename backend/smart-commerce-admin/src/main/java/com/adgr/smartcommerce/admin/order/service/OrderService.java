package com.adgr.smartcommerce.admin.order.service;

import com.adgr.smartcommerce.admin.order.dto.OrderCreateRequest;
import com.adgr.smartcommerce.admin.order.dto.OrderDetailResponse;
import com.adgr.smartcommerce.admin.order.entity.OrderInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OrderService extends IService<OrderInfo> {

    OrderDetailResponse createOrder(Long userId, OrderCreateRequest request);

    IPage<OrderInfo> pageUserOrders(Long userId, long current, long size, Integer orderStatus);

    OrderDetailResponse getUserOrderDetail(Long userId, Long orderId);

    IPage<OrderInfo> pageAdminOrders(
            long current, long size, String orderNo, Long userId, Integer orderStatus);

    OrderDetailResponse getAdminOrderDetail(Long orderId);
}
