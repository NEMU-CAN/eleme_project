package com.iteleme.backend.exception;

import com.iteleme.backend.entity.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;

/**
 * 全局异常处理器，将异常统一封装为 `Result` 响应。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理业务层主动抛出的异常。
     *
     * @param exception 业务异常
     * @return 统一失败响应
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Result> handleApiException(ApiException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(Result.error(exception.getCode(), exception.getMessage(), exception.getDetails()));
    }

    /**
     * 处理请求参数类型错误。
     *
     * @param exception 类型转换异常
     * @return 统一失败响应
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String field = exception.getName();
        return badRequest(field, "参数类型不正确");
    }

    /**
     * 处理缺少必要请求参数的错误。
     *
     * @param exception 缺少参数异常
     * @return 统一失败响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result> handleMissingParameter(MissingServletRequestParameterException exception) {
        return badRequest(exception.getParameterName(), "缺少必要参数");
    }

    /**
     * 处理 JSON 请求体格式错误。
     *
     * @return 统一失败响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result> handleUnreadableBody() {
        return badRequest("body", "请求体格式不正确");
    }

    /**
     * 处理未预期的系统异常。
     *
     * @return 统一失败响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result> handleUnexpectedException() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(50001, "服务器内部错误"));
    }

    /**
     * 构造参数校验失败响应。
     *
     * @param field 参数名
     * @param reason 错误原因
     * @return 统一失败响应
     */
    private ResponseEntity<Result> badRequest(String field, String reason) {
        return ResponseEntity
                .badRequest()
                .body(Result.error(40001, "参数校验失败", List.of(Map.of("field", field, "reason", reason))));
    }
}
