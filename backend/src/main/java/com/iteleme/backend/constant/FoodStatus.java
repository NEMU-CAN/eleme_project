package com.iteleme.backend.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FoodStatus implements CodeEnum {
    OFFLINE(0),
    ONLINE(1);

    private final int code;

    FoodStatus(int code) {
        this.code = code;
    }

    @Override
    @JsonValue
    public int getCode() {
        return code;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static FoodStatus fromCode(Integer code) {
        return CodeEnum.fromCode(FoodStatus.class, code);
    }
}
