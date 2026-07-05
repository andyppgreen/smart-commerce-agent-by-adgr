package com.adgr.smartcommerce.admin.user.service.impl;

import com.adgr.smartcommerce.admin.user.entity.SysUser;
import com.adgr.smartcommerce.admin.user.mapper.SysUserMapper;
import com.adgr.smartcommerce.admin.user.service.SysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public SysUser findByUsername(String username) {
        return lambdaQuery()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDeleted, 0)
                .one();
    }

    @Override
    public boolean updateLastLoginTime(Long userId, LocalDateTime lastLoginTime) {
        return lambdaUpdate()
                .eq(SysUser::getId, userId)
                .set(SysUser::getLastLoginTime, lastLoginTime)
                .update();
    }
}
