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
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("查询用户 - 返回 Result 包装的 UserVO")
    void getUserSuccess() throws Exception {
        mockMvc.perform(get("/api/users/11111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data.id").value("11111111111"))
                .andExpect(jsonPath("$.data.name").value("张三丰"))
                .andExpect(jsonPath("$.data.sex").value(1))
                .andExpect(jsonPath("$.data.delFlag").value(1))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("用户注册 - 首次注册成功返回201和 Result")
    void registerSuccess() throws Exception {
        String json = "{\"userId\":\"u99999\",\"password\":\"pass@123456\",\"userName\":\"测试用户\",\"userSex\":1}";
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value("u99999"))
                .andExpect(jsonPath("$.data.name").value("测试用户"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("用户注册 - 用户已存在返回409和统一错误体")
    void registerConflict() throws Exception {
        String json = "{\"userId\":\"11111111111\",\"password\":\"pass@123456\",\"userName\":\"重复用户\",\"userSex\":1}";
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40901))
                .andExpect(jsonPath("$.data[0].field").value("userId"));
    }

    @Test
    @DisplayName("用户登录 - 正确密码返回 Result 包装的 UserVO")
    void loginSuccess() throws Exception {
        String json = "{\"userId\":\"11111111111\",\"password\":\"123\"}";
        mockMvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value("11111111111"))
                .andExpect(jsonPath("$.data.token").doesNotExist());
    }

    @Test
    @DisplayName("用户登录 - 密码错误返回401")
    void loginWrongPassword() throws Exception {
        String json = "{\"userId\":\"11111111111\",\"password\":\"wrongpassword\"}";
        mockMvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40101))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
