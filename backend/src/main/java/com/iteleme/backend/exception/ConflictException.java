package com.iteleme.backend.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, 40901, message);
    }

    public ConflictException(String message, Object data) {
        super(HttpStatus.CONFLICT, 40901, message, data);
    }
}
