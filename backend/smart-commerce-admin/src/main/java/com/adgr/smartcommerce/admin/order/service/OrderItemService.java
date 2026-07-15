package com.adgr.smartcommerce.admin.order.service;

import com.adgr.smartcommerce.admin.order.entity.OrderItem;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface OrderItemService extends IService<OrderItem> {

    List<OrderItem> listByOrderId(Long orderId);
}
