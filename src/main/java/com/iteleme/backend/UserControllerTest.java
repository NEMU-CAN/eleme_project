package com.iteleme.backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 测试完成后自动回滚，不污染数据库
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("用户注册 - 首次注册成功应返回201")
    void testRegister_Success() throws Exception {
        String json = """
            {
                "userId": "u99999",
                "password": "pass@123456",
                "userName": "测试用户",
                "userSex": 1
            }
            """;
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("u99999"))
                .andExpect(jsonPath("$.userName").value("测试用户"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("用户注册 - 用户已存在应返回409")
    void testRegister_Conflict() throws Exception {
        // u10001 是我们在数据库中预存的张三
        String json = """
            {
                "userId": "u10001",
                "password": "pass@123456",
                "userName": "重复张三",
                "userSex": 1
            }
            """;
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("用户登录 - 账号密码正确应返回200及token")
    void testLogin_Success() throws Exception {
        String json = """
            {
                "userId": "u10001",
                "password": "12345678"
            }
            """;
        mockMvc.perform(post("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("u10001"))
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("用户登录 - 密码错误应返回401")
    void testLogin_WrongPassword() throws Exception {
        String json = """
            {
                "userId": "u10001",
                "password": "wrongpassword"
            }
            """;
        mockMvc.perform(post("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized());
    }
}