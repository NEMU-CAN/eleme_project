package com.iteleme.backend.config;

import com.iteleme.backend.constant.BusinessStatus;
import com.iteleme.backend.constant.FoodStatus;
import com.iteleme.backend.constant.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CodeEnumConverterFactoryTest {
    private final CodeEnumConverterFactory factory = new CodeEnumConverterFactory();

    @Test
    void shouldConvertNumericQueryParameters() {
        Converter<String, BusinessStatus> businessConverter = factory.getConverter(BusinessStatus.class);
        Converter<String, FoodStatus> foodConverter = factory.getConverter(FoodStatus.class);
        Converter<String, OrderStatus> orderConverter = factory.getConverter(OrderStatus.class);

        assertSame(BusinessStatus.DELETED, businessConverter.convert("-1"));
        assertSame(FoodStatus.ONLINE, foodConverter.convert("1"));
        assertSame(OrderStatus.COMPLETED, orderConverter.convert("2"));
    }

    @Test
    void shouldRejectUnknownOrNamedQueryParameters() {
        Converter<String, BusinessStatus> converter = factory.getConverter(BusinessStatus.class);

        assertThrows(IllegalArgumentException.class, () -> converter.convert("99"));
        assertThrows(IllegalArgumentException.class, () -> converter.convert("OPEN"));
    }
}
