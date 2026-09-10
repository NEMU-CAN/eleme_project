package com.iteleme.backend.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, 40301, message);
    }

    public ForbiddenException(String message, Object data) {
        super(HttpStatus.FORBIDDEN, 40301, message, data);
    }
}
