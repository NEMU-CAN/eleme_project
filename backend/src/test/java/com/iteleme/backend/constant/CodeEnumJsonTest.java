package com.iteleme.backend.constant;

import com.iteleme.backend.dto.BusinessStatusRequest;
import com.iteleme.backend.dto.DeliveryAddressSaveRequest;
import com.iteleme.backend.dto.FoodStatusRequest;
import com.iteleme.backend.dto.OrderStatusRequest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CodeEnumJsonTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldSerializeEnumsAsNumbers() throws Exception {
        assertEquals("1", objectMapper.writeValueAsString(BusinessStatus.OPEN));
        assertEquals("0", objectMapper.writeValueAsString(FoodStatus.OFFLINE));
        assertEquals("2", objectMapper.writeValueAsString(GenderType.FEMALE));
        assertEquals("-1", objectMapper.writeValueAsString(OrderStatus.CANCELED));
        assertEquals("2", objectMapper.writeValueAsString(UserRole.ADMIN));
    }

    @Test
    void shouldDeserializeNumericRequestBodies() throws Exception {
        BusinessStatusRequest business = objectMapper.readValue(
                "{\"status\":1}", BusinessStatusRequest.class);
        FoodStatusRequest food = objectMapper.readValue(
                "{\"status\":0}", FoodStatusRequest.class);
        OrderStatusRequest order = objectMapper.readValue(
                "{\"orderStatus\":2}", OrderStatusRequest.class);

        assertSame(BusinessStatus.OPEN, business.status());
        assertSame(FoodStatus.OFFLINE, food.status());
        assertSame(OrderStatus.COMPLETED, order.orderStatus());
    }

    @Test
    void shouldKeepContactSexAliasCompatible() throws Exception {
        DeliveryAddressSaveRequest request = objectMapper.readValue("""
                {
                  "address": "测试地址",
                  "contactName": "测试用户",
                  "contactTel": "13800000000",
                  "contactSex": 1
                }
                """, DeliveryAddressSaveRequest.class);

        assertSame(GenderType.MALE, request.contactGender());
    }
}
