package com.iteleme.backend.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole implements CodeEnum {
    CUSTOMER(0),
    BUSINESS(1),
    ADMIN(2);

    private final int code;

    UserRole(int code) {
        this.code = code;
    }

    @Override
    @JsonValue
    public int getCode() {
        return code;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static UserRole fromCode(Integer code) {
        return CodeEnum.fromCode(UserRole.class, code);
    }
}
