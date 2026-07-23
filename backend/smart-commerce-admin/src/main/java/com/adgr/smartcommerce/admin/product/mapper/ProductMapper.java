package com.adgr.smartcommerce.admin.product.mapper;

import com.adgr.smartcommerce.admin.product.entity.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Update("""
            UPDATE product
            SET stock = stock - #{quantity}, sales = sales + #{quantity}, version = version + 1
            WHERE id = #{productId}
              AND stock >= #{quantity}
              AND status = 1
              AND deleted = 0
            """)
    int deductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
