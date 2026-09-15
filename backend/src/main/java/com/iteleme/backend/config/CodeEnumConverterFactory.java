package com.iteleme.backend.config;

import com.iteleme.backend.constant.CodeEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * 支持在 URL 查询参数中继续使用整数枚举编码。
 */
public class CodeEnumConverterFactory implements ConverterFactory<String, CodeEnum> {
    @Override
    public <T extends CodeEnum> Converter<String, T> getConverter(Class<T> targetType) {
        if (!targetType.isEnum()) {
            throw new IllegalArgumentException(targetType.getName() + " 不是枚举类型");
        }
        return source -> {
            int code = Integer.parseInt(source);
            for (T value : targetType.getEnumConstants()) {
                if (value.getCode() == code) {
                    return value;
                }
            }
            throw new IllegalArgumentException("非法的" + targetType.getSimpleName() + "编码: " + source);
        };
    }
}
