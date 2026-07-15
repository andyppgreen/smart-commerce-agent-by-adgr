package com.adgr.smartcommerce.admin.order.service.impl;

import com.adgr.smartcommerce.admin.common.exception.BusinessException;
import com.adgr.smartcommerce.admin.common.response.ResultCode;
import com.adgr.smartcommerce.admin.order.dto.OrderCreateRequest;
import com.adgr.smartcommerce.admin.order.dto.OrderDetailResponse;
import com.adgr.smartcommerce.admin.order.dto.OrderItemRequest;
import com.adgr.smartcommerce.admin.order.entity.OrderInfo;
import com.adgr.smartcommerce.admin.order.entity.OrderItem;
import com.adgr.smartcommerce.admin.order.mapper.OrderInfoMapper;
import com.adgr.smartcommerce.admin.order.service.OrderItemService;
import com.adgr.smartcommerce.admin.order.service.OrderService;
import com.adgr.smartcommerce.admin.product.entity.Product;
import com.adgr.smartcommerce.admin.product.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {

    private static final DateTimeFormatter ORDER_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final BigDecimal MAX_ORDER_AMOUNT = new BigDecimal("99999999.99");

    private final ProductService productService;
    private final OrderItemService orderItemService;

    public OrderServiceImpl(ProductService productService, OrderItemService orderItemService) {
        this.productService = productService;
        this.orderItemService = orderItemService;
    }

    @Override
    @Transactional
    public OrderDetailResponse createOrder(Long userId, OrderCreateRequest request) {
        Map<Long, Integer> quantities = mergeQuantities(request.items());
        List<Product> products = productService.listByIds(quantities.keySet());
        if (products.size() != quantities.size()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "订单中存在无效商品");
        }

        Map<Long, Product> productMap = new HashMap<>();
        for (Product product : products) {
            productMap.put(product.getId(), product);
        }

        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Product product = productMap.get(entry.getKey());
            validateOrderProduct(product);
            Integer quantity = entry.getValue();
            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_UP);

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getProductName());
            item.setProductImage(product.getMainImage());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(quantity);
            item.setTotalPrice(itemTotal);
            items.add(item);
            totalAmount = totalAmount.add(itemTotal);
        }
        if (totalAmount.compareTo(MAX_ORDER_AMOUNT) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "订单金额超过系统允许范围");
        }

        OrderInfo order = new OrderInfo();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setOrderStatus(0);
        order.setPayStatus(0);
        order.setSourceType(1);
        order.setReceiverName(request.receiverName().trim());
        order.setReceiverPhone(request.receiverPhone().trim());
        order.setReceiverAddress(request.receiverAddress().trim());
        save(order);

        for (OrderItem item : items) {
            if (!productService.deductStock(item.getProductId(), item.getQuantity())) {
                throw new BusinessException(
                        ResultCode.BAD_REQUEST,
                        item.getProductName() + "库存不足或已下架");
            }
            item.setOrderId(order.getId());
        }
        orderItemService.saveBatch(items);
        return buildDetail(getById(order.getId()));
    }

    @Override
    public IPage<OrderInfo> pageUserOrders(
            Long userId, long current, long size, Integer orderStatus) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getUserId, userId)
                .eq(orderStatus != null, OrderInfo::getOrderStatus, orderStatus)
                .orderByDesc(OrderInfo::getId);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public OrderDetailResponse getUserOrderDetail(Long userId, Long orderId) {
        OrderInfo order = lambdaQuery()
                .eq(OrderInfo::getId, orderId)
                .eq(OrderInfo::getUserId, userId)
                .one();
        return buildDetail(order);
    }

    @Override
    public IPage<OrderInfo> pageAdminOrders(
            long current, long size, String orderNo, Long userId, Integer orderStatus) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<OrderInfo>()
                .like(StringUtils.hasText(orderNo), OrderInfo::getOrderNo, orderNo == null ? null : orderNo.trim())
                .eq(userId != null, OrderInfo::getUserId, userId)
                .eq(orderStatus != null, OrderInfo::getOrderStatus, orderStatus)
                .orderByDesc(OrderInfo::getId);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public OrderDetailResponse getAdminOrderDetail(Long orderId) {
        return buildDetail(getById(orderId));
    }

    private OrderDetailResponse buildDetail(OrderInfo order) {
        if (order == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "订单不存在");
        }
        return OrderDetailResponse.from(order, orderItemService.listByOrderId(order.getId()));
    }

    private Map<Long, Integer> mergeQuantities(List<OrderItemRequest> requestedItems) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderItemRequest item : requestedItems) {
            quantities.merge(item.productId(), item.quantity(), Integer::sum);
            if (quantities.get(item.productId()) > 999) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "单个商品购买数量不能超过999");
            }
        }
        return quantities;
    }

    private void validateOrderProduct(Product product) {
        if (product == null
                || !Integer.valueOf(0).equals(product.getDeleted())
                || !Integer.valueOf(1).equals(product.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "订单中存在已下架商品");
        }
    }

    private String generateOrderNo() {
        int randomSuffix = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return "OD" + LocalDateTime.now().format(ORDER_TIME_FORMATTER) + randomSuffix;
    }
}
