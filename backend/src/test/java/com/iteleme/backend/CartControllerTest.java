package com.iteleme.backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CartControllerTest {
    private static final String CART_URL = "/api/users/11111111111/cart-items";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("购物车完整流程 - 使用用户路径并统一返回 Result")
    void cartLifecycle() throws Exception {
        String addJson = "{\"businessId\":10001,\"foodId\":1,\"quantity\":2}";
        String response = mockMvc.perform(post(CART_URL)
                        .contentType(MediaType.APPLICATION_JSON).content(addJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.userId").value("11111111111"))
                .andExpect(jsonPath("$.data.quantity").value(2))
                .andExpect(jsonPath("$.data.business.id").value(10001))
                .andExpect(jsonPath("$.data.business.name").exists())
                .andExpect(jsonPath("$.data.food.id").value(1))
                .andExpect(jsonPath("$.data.food.name").exists())
                .andReturn().getResponse().getContentAsString();

        int idStart = response.indexOf("\"id\":") + 5;
        int idEnd = response.indexOf(',', idStart);
        int cartId = Integer.parseInt(response.substring(idStart, idEnd));

        mockMvc.perform(get(CART_URL).param("businessId", "10001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(cartId));

        mockMvc.perform(patch(CART_URL + "/" + cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(3));

        mockMvc.perform(delete(CART_URL + "/" + cartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("加入购物车 - 未传数量时默认为1")
    void addWithDefaultQuantity() throws Exception {
        String json = "{\"businessId\":10001,\"foodId\":1}";
        mockMvc.perform(post(CART_URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(1));
    }

    @Test
    @DisplayName("加入购物车 - 非法数量返回400错误明细")
    void addWithInvalidQuantity() throws Exception {
        String json = "{\"businessId\":10001,\"foodId\":1,\"quantity\":0}";
        mockMvc.perform(post(CART_URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.data[0].field").value("quantity"));
    }

    @Test
    @DisplayName("加入购物车 - 不存在的商家或食品返回404")
    void addNotFound() throws Exception {
        String json = "{\"businessId\":99999,\"foodId\":99999,\"quantity\":1}";
        mockMvc.perform(post(CART_URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401));
    }

    @Test
    @DisplayName("按条件删除购物车 - 返回200统一响应")
    void deleteByFilter() throws Exception {
        String json = "{\"businessId\":10001,\"foodId\":1,\"quantity\":1}";
        mockMvc.perform(post(CART_URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk());

        mockMvc.perform(delete(CART_URL).param("businessId", "10001").param("foodId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
