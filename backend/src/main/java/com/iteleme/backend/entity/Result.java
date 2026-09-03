package com.iteleme.backend.entity;

import lombok.Data;

/**
 * 统一接口响应体。
 */
@Data
public class Result {

    /** 业务状态码，1 表示成功，其余值表示不同的失败或异常类型。 */
    private Integer code;
    /** 响应提示信息。 */
    private String msg;
    /** 响应主体数据。 */
    private Object data;

    /**
     * 创建成功响应，不携带业务数据。
     *
     * @return 成功响应
     */
    public static Result success() {
        return success(null);
    }

    /**
     * 创建成功响应，并携带业务数据。
     *
     * @param object 业务数据
     * @return 成功响应
     */
    public static Result success(Object object) {
        Result result = new Result();
        result.code = 1;
        result.msg = "success";
        result.data = object;
        return result;
    }

    /**
     * 创建失败响应，仅返回错误信息。
     *
     * @param msg 错误提示
     * @return 失败响应
     */
    public static Result error(String msg) {
        return error(0, msg, null);
    }

    /**
     * 创建失败响应，允许自定义业务码。
     *
     * @param code 业务码
     * @param msg 错误提示
     * @return 失败响应
     */
    public static Result error(Integer code, String msg) {
        return error(code, msg, null);
    }

    /**
     * 创建失败响应，允许自定义业务码和附加数据。
     *
     * @param code 业务码
     * @param msg 错误提示
     * @param object 附加数据
     * @return 失败响应
     */
    public static Result error(Integer code, String msg, Object object) {
        Result result = new Result();
        result.code = code;
        result.msg = msg;
        result.data = object;
        return result;
    }
}
