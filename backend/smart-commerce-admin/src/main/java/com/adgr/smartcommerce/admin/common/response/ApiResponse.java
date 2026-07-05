package com.adgr.smartcommerce.admin.common.response;

public record ApiResponse<T>(int code, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> ApiResponse<T> fail(ResultCode resultCode, String message) {
        return new ApiResponse<>(resultCode.getCode(), message, null);
    }
}
