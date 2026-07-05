package com.adgr.smartcommerce.admin.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({
        "com.adgr.smartcommerce.admin.user.mapper",
        "com.adgr.smartcommerce.admin.role.mapper",
        "com.adgr.smartcommerce.admin.userrole.mapper"
})
public class MyBatisPlusConfig {
}
