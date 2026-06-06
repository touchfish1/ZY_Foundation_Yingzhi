package com.zhangyuan.common.exception;

public enum ErrorCode {

    // General
    INTERNAL_ERROR(500, "Internal server error"),
    BAD_REQUEST(400, "Bad request"),
    NOT_FOUND(404, "Resource not found"),

    // Auth
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),

    // Business
    VALIDATION_FAILED(1001, "Validation failed"),
    DUPLICATE_RESOURCE(1002, "Resource already exists"),
    INSUFFICIENT_BALANCE(1003, "Insufficient balance"),
    INVALID_STATE(1004, "Invalid state transition");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
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
