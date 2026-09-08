package com.iteleme.backend.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, 40101, message);
    }

    public UnauthorizedException(String message, Object data) {
        super(HttpStatus.UNAUTHORIZED, 40101, message, data);
    }
}
