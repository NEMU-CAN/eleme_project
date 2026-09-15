package com.iteleme.backend.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CodeEnumTest {

    @Test
    void shouldKeepOriginalIntegerCodes() {
        assertEquals(0, BusinessStatus.CLOSED.getCode());
        assertEquals(1, BusinessStatus.OPEN.getCode());
        assertEquals(-1, BusinessStatus.DELETED.getCode());

        assertEquals(0, FoodStatus.OFFLINE.getCode());
        assertEquals(1, FoodStatus.ONLINE.getCode());

        assertEquals(0, GenderType.SECRET.getCode());
        assertEquals(1, GenderType.MALE.getCode());
        assertEquals(2, GenderType.FEMALE.getCode());

        assertEquals(-1, OrderStatus.CANCELED.getCode());
        assertEquals(0, OrderStatus.UNPAID.getCode());
        assertEquals(1, OrderStatus.PAID.getCode());
        assertEquals(2, OrderStatus.COMPLETED.getCode());

        assertEquals(0, UserRole.CUSTOMER.getCode());
        assertEquals(1, UserRole.BUSINESS.getCode());
        assertEquals(2, UserRole.ADMIN.getCode());
    }

    @Test
    void shouldResolveEnumsFromIntegerCodes() {
        assertSame(BusinessStatus.DELETED, BusinessStatus.fromCode(-1));
        assertSame(FoodStatus.ONLINE, FoodStatus.fromCode(1));
        assertSame(GenderType.FEMALE, GenderType.fromCode(2));
        assertSame(OrderStatus.PAID, OrderStatus.fromCode(1));
        assertSame(UserRole.ADMIN, UserRole.fromCode(2));
    }

    @Test
    void shouldPreserveNullForOptionalEnumFields() {
        assertNull(BusinessStatus.fromCode(null));
        assertNull(FoodStatus.fromCode(null));
        assertNull(GenderType.fromCode(null));
        assertNull(OrderStatus.fromCode(null));
        assertNull(UserRole.fromCode(null));
    }

    @Test
    void shouldRejectUnknownCodes() {
        assertThrows(IllegalArgumentException.class, () -> BusinessStatus.fromCode(99));
        assertThrows(IllegalArgumentException.class, () -> FoodStatus.fromCode(-1));
        assertThrows(IllegalArgumentException.class, () -> GenderType.fromCode(3));
        assertThrows(IllegalArgumentException.class, () -> OrderStatus.fromCode(3));
        assertThrows(IllegalArgumentException.class, () -> UserRole.fromCode(-1));
    }
}
