package com.adgr.smartcommerce.admin.common.response;

public enum ResultCode {

    SUCCESS(0, "success"),
    BAD_REQUEST(40000, "参数校验失败"),
    LOGIN_FAILED(40100, "用户名或密码错误"),
    UNAUTHORIZED(40101, "未登录或登录已过期"),
    FORBIDDEN(40300, "无权限访问"),
    ACCOUNT_DISABLED(40301, "账号已被禁用"),
    SYSTEM_ERROR(50000, "系统异常");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
