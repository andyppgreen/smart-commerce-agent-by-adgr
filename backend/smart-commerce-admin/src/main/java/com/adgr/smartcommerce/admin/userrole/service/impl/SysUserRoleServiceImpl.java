package com.adgr.smartcommerce.admin.userrole.service.impl;

import com.adgr.smartcommerce.admin.userrole.entity.SysUserRole;
import com.adgr.smartcommerce.admin.userrole.mapper.SysUserRoleMapper;
import com.adgr.smartcommerce.admin.userrole.service.SysUserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

    @Override
    public List<Long> findRoleIdsByUserId(Long userId) {
        return lambdaQuery()
                .eq(SysUserRole::getUserId, userId)
                .list()
                .stream()
                .map(SysUserRole::getRoleId)
                .filter(Objects::nonNull)
                .toList();
    }
}
