package com.iteleme.backend.service.impl;

import com.iteleme.backend.exception.ApiException;

/**
 * 服务层通用参数校验工具。
 */
final class ServiceValidator {
    private ServiceValidator() {
    }

    /**
     * 校验用户编号。
     */
    static void requireUserId(String userId) {
        requireNonBlank(userId, "userId");
        requireMaxLength(userId, "userId", 20);
    }

    /**
     * 校验值必须为正数。
     */
    static void requirePositive(Integer value, String field) {
        if (value == null || value <= 0) {
            throw ApiException.badRequest(field, "必须大于 0");
        }
    }

    /**
     * 校验可选值在存在时必须为正数。
     */
    static void requireOptionalPositive(Integer value, String field) {
        if (value != null && value <= 0) {
            throw ApiException.badRequest(field, "必须大于 0");
        }
    }

    /**
     * 校验值只能是 0 或 1。
     */
    static void requireZeroOrOne(Integer value, String field) {
        if (value == null || (value != 0 && value != 1)) {
            throw ApiException.badRequest(field, "只允许 0 或 1");
        }
    }

    /**
     * 校验可选值在存在时只能是 0 或 1。
     */
    static void requireOptionalZeroOrOne(Integer value, String field) {
        if (value != null && value != 0 && value != 1) {
            throw ApiException.badRequest(field, "只允许 0 或 1");
        }
    }

    /**
     * 校验字符串不能为空白。
     */
    static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw ApiException.badRequest(field, "不能为空");
        }
    }

    /**
     * 校验字符串长度不超过上限。
     */
    static void requireMaxLength(String value, String field, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw ApiException.badRequest(field, "长度不能超过 " + maxLength);
        }
    }
}
