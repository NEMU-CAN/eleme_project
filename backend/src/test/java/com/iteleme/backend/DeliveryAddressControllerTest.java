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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DeliveryAddressControllerTest {
    private static final String ADDRESS_URL = "/api/users/11111111111/delivery-addresses";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("查询地址列表 - 使用用户路径并返回 Result")
    void listDeliveryAddresses() throws Exception {
        mockMvc.perform(get(ADDRESS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @DisplayName("获取地址详情 - 返回规范 DeliveryAddressVO")
    void getDeliveryAddressSuccess() throws Exception {
        mockMvc.perform(get(ADDRESS_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.contactName").value("张三丰"))
                .andExpect(jsonPath("$.data.userId").value("11111111111"));
    }

    @Test
    @DisplayName("获取地址详情 - 不存在时返回404")
    void getDeliveryAddressNotFound() throws Exception {
        mockMvc.perform(get(ADDRESS_URL + "/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401));
    }

    @Test
    @DisplayName("新增和更新收货地址 - 返回规范 Result")
    void createAndUpdateDeliveryAddress() throws Exception {
        String createJson = "{\"contactName\":\"李四\",\"contactSex\":1,\"contactTel\":\"13900001234\",\"address\":\"天津市南开区卫津路92号\"}";
        String response = mockMvc.perform(post(ADDRESS_URL)
                        .contentType(MediaType.APPLICATION_JSON).content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.contactName").value("李四"))
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn().getResponse().getContentAsString();

        int idStart = response.indexOf("\"id\":") + 5;
        int idEnd = response.indexOf(',', idStart);
        int daId = Integer.parseInt(response.substring(idStart, idEnd));
        String updateJson = "{\"contactName\":\"李四\",\"contactSex\":1,\"contactTel\":\"13900009999\",\"address\":\"天津市南开区卫津路93号\"}";

        mockMvc.perform(put(ADDRESS_URL + "/" + daId)
                        .contentType(MediaType.APPLICATION_JSON).content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contactTel").value("13900009999"));
    }

    @Test
    @DisplayName("删除收货地址 - 存在时返回200统一响应")
    void deleteDeliveryAddressSuccess() throws Exception {
        mockMvc.perform(delete(ADDRESS_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
