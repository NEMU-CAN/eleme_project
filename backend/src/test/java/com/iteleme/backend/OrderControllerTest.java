package com.iteleme.backend;

import com.iteleme.backend.config.JwtUtil;
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
    private static final String ADDRESS_URL = "/api/users/" + USER_ID + "/delivery-addresses";

    @Autowired
    private MockMvc mockMvc;
    // ===== [阶段② 新增] 生成鉴权 token =====
    @Autowired
    private JwtUtil jwtUtil;

    private String auth(String userId) {
        return "Bearer " + jwtUtil.generateToken(userId);
    }
    // ===== [阶段② 新增结束] =====

    @Test
    @DisplayName("创建、查询订单 - 兼容原始 elm 数据库字段")
    void createAndQueryOrder() throws Exception {
        mockMvc.perform(post(CART_URL)
                        .header("Authorization", auth(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessId\":10001,\"foodId\":1,\"quantity\":2}"))
                .andExpect(status().isOk());

        String response = mockMvc.perform(post(ORDER_URL)
                        .header("Authorization", auth(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessId\":10001,\"daId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.userId").value(USER_ID))
                .andExpect(jsonPath("$.data.businessId").value(10001))
                .andExpect(jsonPath("$.data.addressId").value(1))
                .andExpect(jsonPath("$.data.orderStatus").value(0))
                .andExpect(jsonPath("$.data.orderTotal").value(35.0))
                .andExpect(jsonPath("$.data.business.id").value(10001))
                .andExpect(jsonPath("$.data.deliveryAddress.id").value(1))
                .andExpect(jsonPath("$.data.items[0].food.id").value(1))
                .andReturn().getResponse().getContentAsString();

        int idStart = response.indexOf("\"id\":") + 5;
        int idEnd = response.indexOf(',', idStart);
        int orderId = Integer.parseInt(response.substring(idStart, idEnd));

        mockMvc.perform(get(ORDER_URL).header("Authorization", auth(USER_ID)).param("businessId", "10001").param("orderState", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(orderId));

        mockMvc.perform(get(ORDER_URL + "/" + orderId).header("Authorization", auth(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(orderId));

        mockMvc.perform(get(CART_URL).header("Authorization", auth(USER_ID)).param("businessId", "10001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("创建订单 - 购物车为空返回409")
    void createOrderWithEmptyCart() throws Exception {
        mockMvc.perform(post(ORDER_URL)
                        .header("Authorization", auth(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessId\":10001,\"daId\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40901))
                .andExpect(jsonPath("$.data[0].field").value("cart"));
    }

    @Test
    @DisplayName("创建订单 - 收货地址不存在返回404")
    void createOrderWithMissingAddressReturns404() throws Exception {
        mockMvc.perform(post(CART_URL)
                        .header("Authorization", auth(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessId\":10001,\"foodId\":1,\"quantity\":1}"))
                .andExpect(status().isOk());

        mockMvc.perform(post(ORDER_URL)
                        .header("Authorization", auth(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessId\":10001,\"daId\":999999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401))
                .andExpect(jsonPath("$.msg").value("资源不存在"));
    }

    @Test
    @DisplayName("创建订单 - 商家不存在返回404")
    void createOrderWithMissingBusinessReturns404() throws Exception {
        mockMvc.perform(post(ORDER_URL)
                        .header("Authorization", auth(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessId\":999999,\"daId\":1}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401));
    }

    @Test
    @DisplayName("订单详情 - 订单不存在返回404")
    void getOrderDetailNotFoundReturns404() throws Exception {
        mockMvc.perform(get(ORDER_URL + "/999999").header("Authorization", auth(USER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401))
                .andExpect(jsonPath("$.msg").value("资源不存在"));
    }

    @Test
    @DisplayName("订单列表 - 用户不存在/已删除的 token 返回401")
    void listOrdersUserNotFoundTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/users/99999999999/orders").header("Authorization", auth("99999999999")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40101))
                .andExpect(jsonPath("$.msg").value("未登录或登录已过期"));
    }

    @Test
    @DisplayName("订单列表 - 无订单返回空数组")
    void listOrdersEmptyReturnsEmptyArray() throws Exception {
        mockMvc.perform(get(ORDER_URL).header("Authorization", auth(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("订单列表 - orderState 非法值返回400")
    void listOrdersInvalidOrderStateReturns400() throws Exception {
        mockMvc.perform(get(ORDER_URL).header("Authorization", auth(USER_ID)).param("orderState", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.data[0].field").value("orderState"));
    }

    // ===== [重构] 收货地址快照：删除地址后订单仍显示下单时地址 =====
    @Test
    @DisplayName("订单地址快照 - 删除地址后订单仍显示下单时地址")
    void orderKeepsAddressSnapshotAfterAddressDeleted() throws Exception {
        // 1. 新建一个收货地址
        String addressResponse = mockMvc.perform(post(ADDRESS_URL)
                        .header("Authorization", auth(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contactName\":\"快照测试\",\"contactSex\":1,\"contactTel\":\"13800001111\",\"address\":\"上海市黄浦区快照路1号\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.contactName").value("快照测试"))
                .andReturn().getResponse().getContentAsString();
        int daIdStart = addressResponse.indexOf("\"id\":") + 5;
        int daIdEnd = addressResponse.indexOf(',', daIdStart);
        int daId = Integer.parseInt(addressResponse.substring(daIdStart, daIdEnd));

        // 2. 加购物车并下单
        mockMvc.perform(post(CART_URL)
                        .header("Authorization", auth(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessId\":10001,\"foodId\":1,\"quantity\":1}"))
                .andExpect(status().isOk());

        String orderResponse = mockMvc.perform(post(ORDER_URL)
                        .header("Authorization", auth(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessId\":10001,\"daId\":" + daId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.deliveryAddress.contactName").value("快照测试"))
                .andReturn().getResponse().getContentAsString();
        int orderIdStart = orderResponse.indexOf("\"id\":") + 5;
        int orderIdEnd = orderResponse.indexOf(',', orderIdStart);
        int orderId = Integer.parseInt(orderResponse.substring(orderIdStart, orderIdEnd));

        // 3. 删除该地址
        mockMvc.perform(delete(ADDRESS_URL + "/" + daId)
                        .header("Authorization", auth(USER_ID)))
                .andExpect(status().isOk());

        // 4. 查订单：deliveryAddress 仍来自快照
        mockMvc.perform(get(ORDER_URL + "/" + orderId)
                        .header("Authorization", auth(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deliveryAddress.id").value(daId))
                .andExpect(jsonPath("$.data.deliveryAddress.contactName").value("快照测试"))
                .andExpect(jsonPath("$.data.deliveryAddress.contactTel").value("13800001111"))
                .andExpect(jsonPath("$.data.deliveryAddress.address").value("上海市黄浦区快照路1号"));
    }
    // ===== [重构结束] =====
}
