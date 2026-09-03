package com.iteleme.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * 业务异常，交给全局异常处理器统一转成 `Result`。
 */
@Getter
public class ApiException extends RuntimeException {
    /** 对应的 HTTP 状态。 */
    private final HttpStatus status;
    /** 业务错误码。 */
    private final Integer code;
    /** 参数校验或冲突时携带的错误明细。 */
    private final List<Map<String, String>> details;

    public ApiException(HttpStatus status, Integer code, String message) {
        this(status, code, message, null);
    }

    public ApiException(HttpStatus status, Integer code, String message, List<Map<String, String>> details) {
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
                List.of(Map.of("field", field, "reason", reason))
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
                List.of(Map.of("field", field, "reason", reason))
        );
    }
}
