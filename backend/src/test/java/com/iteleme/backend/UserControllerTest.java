package com.iteleme.backend;

import com.iteleme.backend.config.JwtUtil;
import com.iteleme.backend.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
    // ===== [阶段② 新增] 生成鉴权 token =====
    @Autowired
    private JwtUtil jwtUtil;
    // ===== [第二步 新增] 单会话：测试 token 需写入 current_token_hash，否则拦截器按哈希校验拒绝 =====
    @Autowired
    private UserMapper userMapper;

    private String auth() {
        String token = jwtUtil.generateToken("11111111111");
        userMapper.updateCurrentTokenHash("11111111111", jwtUtil.hashToken(token));
        return "Bearer " + token;
    }
    // ===== [第二步 新增结束] =====

    @Test
    @DisplayName("查询用户 - 返回 Result 包装的 UserVO")
    void getUserSuccess() throws Exception {
        mockMvc.perform(get("/api/users/11111111111").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data.id").value("11111111111"))
                .andExpect(jsonPath("$.data.name").value("张三丰"))
                .andExpect(jsonPath("$.data.sex").value(1))
                .andExpect(jsonPath("$.data.delFlag").value(1))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    // ===== [阶段② 新增] 强制鉴权：无 token 访问受保护接口返回 401 =====
    @Test
    @DisplayName("查询用户 - 未带 token 返回401")
    void getUserWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/users/11111111111"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40101))
                .andExpect(jsonPath("$.msg").value("未登录或登录已过期"));
    }

    // ===== [阶段② 新增] token 优先：传他人 userId 仍返回 token 用户自己的数据 =====
    @Test
    @DisplayName("查询用户 - token 优先：传他人 userId 返回 token 用户自己的数据")
    void getUserTokenOverridesPathUserId() throws Exception {
        // 用 11111111111 的 token 去查 99999999999（不存在）→ 拦截器用 token 里的 userId 覆盖路径变量
        // 若未覆盖，会因 99999999999 不存在而 404；覆盖后返回 11111111111 自己的数据
        mockMvc.perform(get("/api/users/99999999999").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value("11111111111"))
                .andExpect(jsonPath("$.data.name").value("张三丰"));
    }
    // ===== [阶段② 新增结束] =====

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
    @DisplayName("用户登录 - 正确密码返回 Result 包装的 LoginVO(token+user)")
    void loginSuccess() throws Exception {
        String json = "{\"userId\":\"11111111111\",\"password\":\"123\"}";
        mockMvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                // ===== [阶段① 新增] login 返回 LoginVO{token,user}，token 应存在，用户信息在 user 下 =====
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.id").value("11111111111"))
                .andExpect(jsonPath("$.data.user.name").value("张三丰"))
                .andExpect(jsonPath("$.data.user.password").doesNotExist());
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

    // ===== [第一步 新增] 退出登录（注销会话）基础框架 =====
    @Test
    @DisplayName("退出登录 - 带 token 返回200")
    void logoutSuccess() throws Exception {
        mockMvc.perform(delete("/api/users/11111111111/sessions").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.msg").value("success"));
    }

    @Test
    @DisplayName("退出登录 - 未带 token 返回401")
    void logoutWithoutTokenReturns401() throws Exception {
        mockMvc.perform(delete("/api/users/11111111111/sessions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40101))
                .andExpect(jsonPath("$.msg").value("未登录或登录已过期"));
    }
    // ===== [第一步 新增结束] =====

    // ===== [第二步 新增] token 失效机制（单会话：current_token_hash） =====
    @Test
    @DisplayName("退出登录 - 注销后旧 token 访问返回401")
    void logoutInvalidatesToken() throws Exception {
        String token = auth();
        mockMvc.perform(delete("/api/users/11111111111/sessions").header("Authorization", token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/11111111111").header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40101))
                .andExpect(jsonPath("$.msg").value("未登录或登录已过期"));
    }

    @Test
    @DisplayName("单会话 - 重新登录后旧 token 失效，新 token 可用")
    void newLoginInvalidatesOldToken() throws Exception {
        String loginJson = "{\"userId\":\"11111111111\",\"password\":\"123\"}";
        String first = mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON).content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token1 = new ObjectMapper().readTree(first).path("data").path("token").asText();

        String second = mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON).content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token2 = new ObjectMapper().readTree(second).path("data").path("token").asText();

        mockMvc.perform(get("/api/users/11111111111").header("Authorization", "Bearer " + token1))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40101));
        mockMvc.perform(get("/api/users/11111111111").header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("11111111111"));
    }
    // ===== [第二步 新增结束] =====

    // ===== [第一步 新增] 删除账户（软删） =====
    @Test
    @DisplayName("删除账户 - 删除后同 token 访问返回401")
    void deleteAccountSuccessThenTokenInvalid() throws Exception {
        mockMvc.perform(delete("/api/users/11111111111").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(get("/api/users/11111111111").header("Authorization", auth()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40101));
    }

    @Test
    @DisplayName("删除账户 - 未带 token 返回401")
    void deleteAccountWithoutTokenReturns401() throws Exception {
        mockMvc.perform(delete("/api/users/11111111111"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40101));
    }

    @Test
    @DisplayName("删除账户后重新注册 - 同 userId 恢复并可用新密码登录")
    void reRegisterAfterDelete() throws Exception {
        mockMvc.perform(delete("/api/users/11111111111").header("Authorization", auth()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"11111111111\",\"password\":\"newpass123\",\"userName\":\"重注册用户\",\"userSex\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value("11111111111"))
                .andExpect(jsonPath("$.data.name").value("重注册用户"));

        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"11111111111\",\"password\":\"newpass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.name").value("重注册用户"));
    }
    // ===== [第一步 新增结束] =====
}
