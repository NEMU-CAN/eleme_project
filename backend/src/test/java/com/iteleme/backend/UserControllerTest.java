package com.iteleme.backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
    @DisplayName("用户注册 - 首次注册成功应返回201")
    void registerSuccess() throws Exception {
        String json = """{"userId":"u99999","password":"pass@123456","userName":"测试用户","userSex":1}""";
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("u99999"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("用户注册 - 用户已存在应返回409")
    void registerConflict() throws Exception {
        String json = """{"userId":"u10001","password":"pass@123456","userName":"重复用户","userSex":1}""";
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("用户登录 - 正确密码返回token")
    void loginSuccess() throws Exception {
        String json = """{"userId":"u10001","password":"12345678"}""";
        mockMvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("u10001"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("用户登录 - 密码错误返回401")
    void loginWrongPassword() throws Exception {
        String json = """{"userId":"u10001","password":"wrongpassword"}""";
        mockMvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isUnauthorized());
    }
}
