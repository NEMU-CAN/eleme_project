package com.iteleme.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final Integer code;
    private final Object data;

    protected ApiException(HttpStatus status, Integer code, String message) {
        this(status, code, message, null);
    }

    protected ApiException(HttpStatus status, Integer code, String message, Object data) {
        super(message);
        this.status = status;
        this.code = code;
        this.data = data;
    }
}
