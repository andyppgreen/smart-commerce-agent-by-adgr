package com.adgr.smartcommerce.admin.common.exception;

import com.adgr.smartcommerce.admin.common.response.ApiResponse;
import com.adgr.smartcommerce.admin.common.response.ResultCode;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        HttpStatus status = switch (ex.getResultCode()) {
            case LOGIN_FAILED, UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN, ACCOUNT_DISABLED -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(ApiResponse.fail(ex.getResultCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        String message = ResultCode.BAD_REQUEST.getMessage();
        if (bindingResult.hasFieldErrors()) {
            FieldError fieldError = bindingResult.getFieldErrors().get(0);
            message = fieldError.getField() + ": " + fieldError.getDefaultMessage();
        }
        return ResponseEntity.badRequest().body(ApiResponse.fail(ResultCode.BAD_REQUEST, message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(ResultCode.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(ResultCode.SYSTEM_ERROR, ResultCode.SYSTEM_ERROR.getMessage()));
    }
}
