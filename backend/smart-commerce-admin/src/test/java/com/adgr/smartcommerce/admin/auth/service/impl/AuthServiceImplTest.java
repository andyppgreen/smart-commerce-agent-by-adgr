package com.adgr.smartcommerce.admin.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.adgr.smartcommerce.admin.auth.dto.LoginRequest;
import com.adgr.smartcommerce.admin.auth.dto.LoginResponse;
import com.adgr.smartcommerce.admin.auth.dto.LoginToken;
import com.adgr.smartcommerce.admin.auth.service.JwtTokenService;
import com.adgr.smartcommerce.admin.common.exception.BusinessException;
import com.adgr.smartcommerce.admin.common.response.ResultCode;
import com.adgr.smartcommerce.admin.role.entity.SysRole;
import com.adgr.smartcommerce.admin.role.service.SysRoleService;
import com.adgr.smartcommerce.admin.user.entity.SysUser;
import com.adgr.smartcommerce.admin.user.service.SysUserService;
import com.adgr.smartcommerce.admin.userrole.entity.SysUserRole;
import com.adgr.smartcommerce.admin.userrole.service.SysUserRoleService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SysUserService sysUserService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SysRoleService sysRoleService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SysUserRoleService sysUserRoleService;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void loginShouldReturnTokenAndRoles() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setNickname("后台管理员");
        user.setUserType(1);
        user.setPasswordHash("encoded-password");
        user.setStatus(1);

        when(sysUserService.findByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(sysUserRoleService.findRoleIdsByUserId(1L)).thenReturn(List.of(1L));
        when(sysRoleService.listByIds(List.of(1L)))
                .thenReturn(List.of(role(1L, "ADMIN")));
        when(jwtTokenService.createToken(user, List.of("ADMIN")))
                .thenReturn(new LoginToken("token-123", Instant.parse("2026-07-05T00:00:00Z")));

        LoginResponse response = authService.login(new LoginRequest("admin", "password"));

        assertEquals("token-123", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals("admin", response.user().username());
        assertEquals(List.of("ADMIN"), response.user().roles());
    }

    @Test
    void loginShouldRejectWrongPassword() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setPasswordHash("encoded-password");
        user.setStatus(1);

        when(sysUserService.findByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login(new LoginRequest("admin", "wrong")));

        assertEquals(ResultCode.LOGIN_FAILED, exception.getResultCode());
    }

    private SysRole role(Long id, String roleCode) {
        SysRole sysRole = new SysRole();
        sysRole.setId(id);
        sysRole.setRoleCode(roleCode);
        return sysRole;
    }
}
