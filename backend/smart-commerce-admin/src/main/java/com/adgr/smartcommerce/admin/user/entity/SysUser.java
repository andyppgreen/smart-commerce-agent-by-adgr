package com.adgr.smartcommerce.admin.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String username;
    @TableField("password_hash")
    private String passwordHash;
    private String nickname;
    private String phone;
    private String email;
    @TableField("user_type")
    private Integer userType;
    private Integer status;
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    private Integer deleted;
}
