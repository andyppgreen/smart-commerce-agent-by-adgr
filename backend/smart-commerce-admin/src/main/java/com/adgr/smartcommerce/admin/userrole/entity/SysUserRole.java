package com.adgr.smartcommerce.admin.userrole.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("sys_user_role")
public class SysUserRole {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("role_id")
    private Long roleId;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
