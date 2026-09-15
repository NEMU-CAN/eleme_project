package com.iteleme.backend.constant;

/**
 * 使用整数编码持久化和传输的枚举。
 */
public interface CodeEnum {
    int getCode();

    static <E extends Enum<E> & CodeEnum> E fromCode(Class<E> enumType, Integer code) {
        if (code == null) {
            return null;
        }
        for (E value : enumType.getEnumConstants()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("非法的" + enumType.getSimpleName() + "编码: " + code);
    }
}
