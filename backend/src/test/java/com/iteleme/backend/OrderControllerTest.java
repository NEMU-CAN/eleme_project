package com.iteleme.backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerTest {
    private static final String USER_ID = "11111111111";
    private static final String CART_URL = "/api/users/" + USER_ID + "/cart-items";
    private static final String ORDER_URL = "/api/users/" + USER_ID + "/orders";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("创建、查询订单 - 兼容原始 elm 数据库字段")
    void createAndQueryOrder() throws Exception {
        mockMvc.perform(post(CART_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessId\":10001,\"foodId\":1,\"quantity\":2}"))
                .andExpect(status().isOk());

        String response = mockMvc.perform(post(ORDER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessId\":10001,\"daId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.userId").value(USER_ID))
                .andExpect(jsonPath("$.data.businessId").value(10001))
                .andExpect(jsonPath("$.data.addressId").value(1))
                .andExpect(jsonPath("$.data.orderStatus").value(0))
                .andExpect(jsonPath("$.data.business.id").value(10001))
                .andExpect(jsonPath("$.data.deliveryAddress.id").value(1))
                .andExpect(jsonPath("$.data.items[0].food.id").value(1))
                .andReturn().getResponse().getContentAsString();

        int idStart = response.indexOf("\"id\":") + 5;
        int idEnd = response.indexOf(',', idStart);
        int orderId = Integer.parseInt(response.substring(idStart, idEnd));

        mockMvc.perform(get(ORDER_URL).param("businessId", "10001").param("orderState", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(orderId));

        mockMvc.perform(get(ORDER_URL + "/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(orderId));

        mockMvc.perform(get(CART_URL).param("businessId", "10001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("创建订单 - 购物车为空返回409")
    void createOrderWithEmptyCart() throws Exception {
        mockMvc.perform(post(ORDER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessId\":10001,\"daId\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40901))
                .andExpect(jsonPath("$.data[0].field").value("cart"));
    }
}
