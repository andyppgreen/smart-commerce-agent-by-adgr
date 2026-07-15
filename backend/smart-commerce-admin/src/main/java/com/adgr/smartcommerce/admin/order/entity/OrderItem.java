package com.adgr.smartcommerce.admin.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("order_item")
public class OrderItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("order_id")
    private Long orderId;
    @TableField("product_id")
    private Long productId;
    @TableField("product_name")
    private String productName;
    @TableField("product_image")
    private String productImage;
    @TableField("unit_price")
    private BigDecimal unitPrice;
    private Integer quantity;
    @TableField("total_price")
    private BigDecimal totalPrice;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
