package com.iteleme.backend.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, 40401, message);
    }

    public NotFoundException(String message, Object data) {
        super(HttpStatus.NOT_FOUND, 40401, message, data);
    }
}
