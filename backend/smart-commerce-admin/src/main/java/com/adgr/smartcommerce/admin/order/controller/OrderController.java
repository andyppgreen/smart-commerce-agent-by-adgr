package com.adgr.smartcommerce.admin.order.controller;

import com.adgr.smartcommerce.admin.auth.annotation.RequireRoles;
import com.adgr.smartcommerce.admin.auth.context.LoginUserContext;
import com.adgr.smartcommerce.admin.common.response.ApiResponse;
import com.adgr.smartcommerce.admin.common.response.PageResponse;
import com.adgr.smartcommerce.admin.order.dto.OrderCreateRequest;
import com.adgr.smartcommerce.admin.order.dto.OrderDetailResponse;
import com.adgr.smartcommerce.admin.order.dto.OrderSummaryResponse;
import com.adgr.smartcommerce.admin.order.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/orders")
@RequireRoles("CUSTOMER")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<OrderDetailResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        return ApiResponse.success(orderService.createOrder(currentUserId(), request));
    }

    @GetMapping
    public ApiResponse<PageResponse<OrderSummaryResponse>> list(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") long current,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "每页数量必须大于0")
            @Max(value = 100, message = "每页最多查询100条") long size,
            @RequestParam(required = false)
            @Min(value = 0, message = "订单状态必须在0到4之间")
            @Max(value = 4, message = "订单状态必须在0到4之间") Integer orderStatus) {
        return ApiResponse.success(PageResponse.from(
                orderService.pageUserOrders(currentUserId(), current, size, orderStatus),
                OrderSummaryResponse::from));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(orderService.getUserOrderDetail(currentUserId(), id));
    }

    private Long currentUserId() {
        return LoginUserContext.require().userId();
    }
}
