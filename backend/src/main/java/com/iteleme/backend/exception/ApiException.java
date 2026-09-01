package com.iteleme.backend.exception;

import com.iteleme.backend.result.ErrorDetail;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final Integer code;
    private final List<ErrorDetail> details;

    public ApiException(HttpStatus status, Integer code, String message) {
        this(status, code, message, null);
    }

    public ApiException(HttpStatus status, Integer code, String message, List<ErrorDetail> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details;
    }

    public static ApiException badRequest(String field, String reason) {
        return new ApiException(
                HttpStatus.BAD_REQUEST,
                40001,
                "参数校验失败",
                List.of(new ErrorDetail(field, reason))
        );
    }

    public static ApiException unauthorized() {
        return new ApiException(HttpStatus.UNAUTHORIZED, 40101, "账号或密码错误");
    }

    public static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, 40401, "资源不存在");
    }

    public static ApiException conflict(String field, String reason) {
        return new ApiException(
                HttpStatus.CONFLICT,
                40901,
                "业务状态冲突",
                List.of(new ErrorDetail(field, reason))
        );
    }
}
