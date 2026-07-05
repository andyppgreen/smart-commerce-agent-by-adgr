package com.adgr.smartcommerce.admin.auth.service;

import com.adgr.smartcommerce.admin.auth.dto.LoginRequest;
import com.adgr.smartcommerce.admin.auth.dto.LoginResponse;
import com.adgr.smartcommerce.admin.auth.dto.LoginUserInfo;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginUserInfo currentUser();
}
