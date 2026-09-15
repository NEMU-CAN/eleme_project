package com.iteleme.backend.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus implements CodeEnum {
    CANCELED(-1),
    UNPAID(0),
    PAID(1),
    COMPLETED(2);

    private final int code;

    OrderStatus(int code) {
        this.code = code;
    }

    @Override
    @JsonValue
    public int getCode() {
        return code;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static OrderStatus fromCode(Integer code) {
        return CodeEnum.fromCode(OrderStatus.class, code);
    }
}
