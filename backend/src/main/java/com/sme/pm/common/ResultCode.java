package com.sme.pm.common;

public enum ResultCode {
    SUCCESS(200, "success"),
    PARAM_ERROR(400, "Parameter error"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not found"),
    INTERNAL_ERROR(500, "Internal server error");

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
