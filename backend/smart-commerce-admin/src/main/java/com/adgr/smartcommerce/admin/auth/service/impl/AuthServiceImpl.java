package com.adgr.smartcommerce.admin.auth.service.impl;

import com.adgr.smartcommerce.admin.auth.dto.LoginRequest;
import com.adgr.smartcommerce.admin.auth.dto.LoginResponse;
import com.adgr.smartcommerce.admin.auth.dto.LoginToken;
import com.adgr.smartcommerce.admin.auth.dto.LoginUserInfo;
import com.adgr.smartcommerce.admin.auth.dto.LoginUserPrincipal;
import com.adgr.smartcommerce.admin.auth.context.LoginUserContext;
import com.adgr.smartcommerce.admin.auth.service.AuthService;
import com.adgr.smartcommerce.admin.auth.service.JwtTokenService;
import com.adgr.smartcommerce.admin.common.exception.BusinessException;
import com.adgr.smartcommerce.admin.common.response.ResultCode;
import com.adgr.smartcommerce.admin.role.entity.SysRole;
import com.adgr.smartcommerce.admin.role.service.SysRoleService;
import com.adgr.smartcommerce.admin.user.entity.SysUser;
import com.adgr.smartcommerce.admin.user.service.SysUserService;
import com.adgr.smartcommerce.admin.userrole.entity.SysUserRole;
import com.adgr.smartcommerce.admin.userrole.service.SysUserRoleService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final SysUserRoleService sysUserRoleService;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(SysUserService sysUserService,
                           SysRoleService sysRoleService,
                           SysUserRoleService sysUserRoleService,
                           JwtTokenService jwtTokenService,
                           PasswordEncoder passwordEncoder) {
        this.sysUserService = sysUserService;
        this.sysRoleService = sysRoleService;
        this.sysUserRoleService = sysUserRoleService;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserService.findByUsername(request.username());
        if (user == null) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        List<String> roleCodes = findRoleCodes(user.getId());
        LoginToken loginToken = jwtTokenService.createToken(user, roleCodes);
        sysUserService.updateLastLoginTime(user.getId(), LocalDateTime.now());

        return new LoginResponse(
                loginToken.accessToken(),
                "Bearer",
                loginToken.expiresAt(),
                new LoginUserInfo(
                        user.getId(),
                        user.getUsername(),
                        user.getNickname(),
                        user.getUserType(),
                        roleCodes));
    }

    @Override
    public LoginUserInfo currentUser() {
        LoginUserPrincipal principal = LoginUserContext.require();
        SysUser user = sysUserService.getById(principal.userId());
        if (user == null || user.getDeleted() != null && user.getDeleted() == 1) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        return new LoginUserInfo(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getUserType(),
                findRoleCodes(user.getId()));
    }

    private List<String> findRoleCodes(Long userId) {
        List<Long> roleIds = sysUserRoleService.findRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return sysRoleService.listByIds(roleIds)
                .stream()
                .map(SysRole::getRoleCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
