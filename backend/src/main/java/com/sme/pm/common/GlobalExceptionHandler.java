package com.sme.pm.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        return Result.error(400, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntime(RuntimeException e) {
        return Result.error(500, e.getMessage());
    }
}
