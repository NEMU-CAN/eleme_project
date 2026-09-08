package com.iteleme.backend.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, 40001, message);
    }

    public BadRequestException(String message, Object data) {
        super(HttpStatus.BAD_REQUEST, 40001, message, data);
    }
}
