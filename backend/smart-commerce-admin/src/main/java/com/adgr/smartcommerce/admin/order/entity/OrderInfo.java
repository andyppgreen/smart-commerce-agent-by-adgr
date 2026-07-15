package com.adgr.smartcommerce.admin.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("order_info")
public class OrderInfo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("order_no")
    private String orderNo;
    @TableField("user_id")
    private Long userId;
    @TableField("total_amount")
    private BigDecimal totalAmount;
    @TableField("pay_amount")
    private BigDecimal payAmount;
    @TableField("order_status")
    private Integer orderStatus;
    @TableField("pay_status")
    private Integer payStatus;
    @TableField("source_type")
    private Integer sourceType;
    @TableField("receiver_name")
    private String receiverName;
    @TableField("receiver_phone")
    private String receiverPhone;
    @TableField("receiver_address")
    private String receiverAddress;
    @TableField("pay_time")
    private LocalDateTime payTime;
    @TableField("cancel_time")
    private LocalDateTime cancelTime;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
