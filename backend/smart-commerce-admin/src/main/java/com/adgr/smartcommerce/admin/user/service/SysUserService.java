package com.adgr.smartcommerce.admin.user.service;

import com.adgr.smartcommerce.admin.user.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import java.time.LocalDateTime;

public interface SysUserService extends IService<SysUser> {

    SysUser findByUsername(String username);

    boolean updateLastLoginTime(Long userId, LocalDateTime lastLoginTime);
}
