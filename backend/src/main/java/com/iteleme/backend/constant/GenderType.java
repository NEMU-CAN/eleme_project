package com.iteleme.backend.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GenderType implements CodeEnum {
    SECRET(0),
    MALE(1),
    FEMALE(2);

    private final int code;

    GenderType(int code) {
        this.code = code;
    }

    @Override
    @JsonValue
    public int getCode() {
        return code;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static GenderType fromCode(Integer code) {
        return CodeEnum.fromCode(GenderType.class, code);
    }
}
