package com.adgr.smartcommerce.admin.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("product")
public class Product {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("category_id")
    private Long categoryId;
    @TableField("product_code")
    private String productCode;
    @TableField("product_name")
    private String productName;
    @TableField("main_image")
    private String mainImage;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Integer sales;
    private Integer status;
    @Version
    private Integer version;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    private Integer deleted;
}
