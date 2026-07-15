package com.adgr.smartcommerce.admin.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrderCreateRequest(
        @NotEmpty(message = "订单商品不能为空")
        @Size(max = 20, message = "一个订单最多包含20种商品")
        List<@Valid OrderItemRequest> items,
        @NotBlank(message = "收货人不能为空")
        @Size(max = 50, message = "收货人不能超过50个字符")
        String receiverName,
        @NotBlank(message = "收货手机号不能为空")
        @Size(max = 20, message = "收货手机号不能超过20个字符")
        String receiverPhone,
        @NotBlank(message = "收货地址不能为空")
        @Size(max = 255, message = "收货地址不能超过255个字符")
        String receiverAddress) {
}
