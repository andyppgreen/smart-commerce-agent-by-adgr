package com.adgr.smartcommerce.admin.order.service.impl;

import com.adgr.smartcommerce.admin.order.entity.OrderItem;
import com.adgr.smartcommerce.admin.order.mapper.OrderItemMapper;
import com.adgr.smartcommerce.admin.order.service.OrderItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem>
        implements OrderItemService {

    @Override
    public List<OrderItem> listByOrderId(Long orderId) {
        return lambdaQuery()
                .eq(OrderItem::getOrderId, orderId)
                .orderByAsc(OrderItem::getId)
                .list();
    }
}
