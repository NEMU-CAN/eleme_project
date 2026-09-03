package com.iteleme.backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DeliveryAddressControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("查询地址列表 - 返回200及地址数组")
    void listDeliveryAddresses() throws Exception {
        mockMvc.perform(get("/api/delivery-addresses"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("获取地址详情 - 存在时返回200")
    void getDeliveryAddressSuccess() throws Exception {
        mockMvc.perform(get("/api/delivery-addresses/30001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.daId").value(30001))
                .andExpect(jsonPath("$.contactName").value("张三"));
    }

    @Test
    @DisplayName("获取地址详情 - 不存在时返回404")
    void getDeliveryAddressNotFound() throws Exception {
        mockMvc.perform(get("/api/delivery-addresses/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("新增收货地址 - 合法数据返回201")
    void createDeliveryAddress() throws Exception {
        String json = """{"contactName":"李四","contactSex":1,"contactTel":"13900001234","address":"天津市南开区卫津路92号"}""";
        mockMvc.perform(post("/api/delivery-addresses")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contactName").value("李四"))
                .andExpect(jsonPath("$.daId").exists());
    }

    @Test
    @DisplayName("删除收货地址 - 存在时返回204")
    void deleteDeliveryAddressSuccess() throws Exception {
        mockMvc.perform(delete("/api/delivery-addresses/30001"))
                .andExpect(status().isNoContent());
    }
}
