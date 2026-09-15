package com.iteleme.backend.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BusinessStatus implements CodeEnum {
    CLOSED(0),
    OPEN(1),
    DELETED(-1);

    private final int code;

    BusinessStatus(int code) {
        this.code = code;
    }

    @Override
    @JsonValue
    public int getCode() {
        return code;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static BusinessStatus fromCode(Integer code) {
        return CodeEnum.fromCode(BusinessStatus.class, code);
    }
}
