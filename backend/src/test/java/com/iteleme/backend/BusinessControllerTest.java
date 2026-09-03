package com.iteleme.backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 商家食品接口集成测试。
 *
 * <p>测试前请确认本地数据库已按项目配置完成初始化。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class BusinessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("查询商家食品列表 - 存在时返回200及食品列表")
    void listFoodsByBusinessIdSuccess() throws Exception {
        mockMvc.perform(get("/api/businesses/10001/foods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("纯肉鲜肉（水饺）"))
                .andExpect(jsonPath("$.data[0].description").exists())
                .andExpect(jsonPath("$.data[0].image").exists())
                .andExpect(jsonPath("$.data[0].price").exists())
                .andExpect(jsonPath("$.data[0].businessId").value(10001));
    }

    @Test
    @DisplayName("查询商家食品列表 - 商家不存在时返回404")
    void listFoodsByBusinessIdNotFound() throws Exception {
        mockMvc.perform(get("/api/businesses/99999999/foods"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401));
    }

    @Test
    @DisplayName("查询商家食品列表 - 非法商家编号返回400")
    void listFoodsByBusinessIdWithInvalidId() throws Exception {
        mockMvc.perform(get("/api/businesses/0/foods"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001));
    }
}
