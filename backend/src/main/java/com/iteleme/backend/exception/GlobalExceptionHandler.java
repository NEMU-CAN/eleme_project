package com.iteleme.backend.exception;

import com.iteleme.backend.common.FieldErrorVO;
import com.iteleme.backend.common.Result;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<FieldErrorVO> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .toList();
        return ResponseEntity.badRequest().body(Result.error(40001, "参数校验失败", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result> handleConstraintViolation(ConstraintViolationException e) {
        return ResponseEntity.badRequest().body(Result.error(40001, e.getMessage()));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Result> handleApiException(ApiException e) {
        return ResponseEntity.status(e.getStatus()).body(Result.error(e.getCode(), e.getMessage(), e.getData()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result> handleException(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.internalServerError().body(Result.error(50001, "对不起,操作失败,请联系管理员"));
    }

    private FieldErrorVO toFieldError(FieldError fieldError) {
        return new FieldErrorVO(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
