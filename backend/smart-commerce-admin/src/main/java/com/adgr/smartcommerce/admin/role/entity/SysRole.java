package com.adgr.smartcommerce.admin.role.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("sys_role")
public class SysRole {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("role_code")
    private String roleCode;
    @TableField("role_name")
    private String roleName;
    private Integer status;
    private String remark;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    private Integer deleted;
}
