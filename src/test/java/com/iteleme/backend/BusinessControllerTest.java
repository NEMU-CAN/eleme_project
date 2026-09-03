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
 * 商家相关接口集成测试。
 *
 * <p>测试前请确认本地 MySQL 已创建 elm 数据库，并导入项目提供的 elm.sql。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class BusinessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("查询商家列表 - 不传分类时返回全部商家数组")
    void listBusinessesWithoutFilter() throws Exception {
        mockMvc.perform(get("/api/businesses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].businessId").value(10001))
                .andExpect(jsonPath("$[0].businessName").value("万家饺子（软件园E18店）"))
                .andExpect(jsonPath("$[0].businessImg").exists())
                .andExpect(jsonPath("$[0].orderTypeId").value(1));
    }

    @Test
    @DisplayName("查询商家列表 - 按点餐分类过滤")
    void listBusinessesByOrderTypeId() throws Exception {
        mockMvc.perform(get("/api/businesses").param("orderTypeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderTypeId").value(1));
    }

    @Test
    @DisplayName("查询商家列表 - 不存在的分类返回空数组")
    void listBusinessesWithUnknownOrderTypeId() throws Exception {
        mockMvc.perform(get("/api/businesses").param("orderTypeId", "999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("查询商家列表 - 非法分类编号返回400")
    void listBusinessesWithInvalidOrderTypeId() throws Exception {
        mockMvc.perform(get("/api/businesses").param("orderTypeId", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001));
    }

    @Test
    @DisplayName("获取商家详情 - 存在时返回商家对象")
    void getBusinessById() throws Exception {
        mockMvc.perform(get("/api/businesses/10001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessId").value(10001))
                .andExpect(jsonPath("$.businessName").value("万家饺子（软件园E18店）"));
    }

    @Test
    @DisplayName("获取商家详情 - 不存在时返回404")
    void getBusinessByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/businesses/99999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401));
    }

    @Test
    @DisplayName("查询商家食品列表 - 存在时返回食品数组")
    void listFoodsByBusinessId() throws Exception {
        mockMvc.perform(get("/api/businesses/10001/foods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].foodId").value(1))
                .andExpect(jsonPath("$[0].foodName").value("纯肉鲜肉（水饺）"))
                .andExpect(jsonPath("$[0].foodExplain").exists())
                .andExpect(jsonPath("$[0].foodImg").exists())
                .andExpect(jsonPath("$[0].foodPrice").exists())
                .andExpect(jsonPath("$[0].businessId").value(10001));
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
