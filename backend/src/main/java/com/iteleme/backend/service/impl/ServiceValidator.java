package com.iteleme.backend.service.impl;

import com.iteleme.backend.exception.ApiException;

final class ServiceValidator {
    private ServiceValidator() {
    }

    static void requireUserId(String userId) {
        requireNonBlank(userId, "userId");
        requireMaxLength(userId, "userId", 20);
    }

    static void requirePositive(Integer value, String field) {
        if (value == null || value <= 0) {
            throw ApiException.badRequest(field, "必须大于 0");
        }
    }

    static void requireOptionalPositive(Integer value, String field) {
        if (value != null && value <= 0) {
            throw ApiException.badRequest(field, "必须大于 0");
        }
    }

    static void requireZeroOrOne(Integer value, String field) {
        if (value == null || (value != 0 && value != 1)) {
            throw ApiException.badRequest(field, "只允许 0 或 1");
        }
    }

    static void requireOptionalZeroOrOne(Integer value, String field) {
        if (value != null && value != 0 && value != 1) {
            throw ApiException.badRequest(field, "只允许 0 或 1");
        }
    }

    static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw ApiException.badRequest(field, "不能为空");
        }
    }

    static void requireMaxLength(String value, String field, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw ApiException.badRequest(field, "长度不能超过 " + maxLength);
        }
    }
}
