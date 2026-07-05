package com.adgr.smartcommerce.admin.userrole.service;

import com.adgr.smartcommerce.admin.userrole.entity.SysUserRole;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface SysUserRoleService extends IService<SysUserRole> {

    List<Long> findRoleIdsByUserId(Long userId);
}
