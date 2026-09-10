package com.iteleme.backend.config;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.iteleme.backend.context.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

/**
 * JWT工具类（融合PR23和PR24优点）。
 * 
 * 特性：
 * - 依赖注入：@Component，方便测试和扩展
 * - 可配置：从application.yaml读取secret和过期时间
 * - 过期检查：验证exp字段，拒绝过期token
 * - 签名验证：HMAC-SHA256签名，防篡改
 * - 单会话支持：hash()方法用于单点登录
 * - JSON解析：使用Jackson标准解析
 * - 多角色：支持role字段（0=普通用户, 1=商家, 2=管理员）
 * - Integer userId：类型安全
 */
@Slf4j
@Component
public class JwtUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    private final SecretKeySpec key;
    private final long expireMillis;

    /**
     * 构造器：依赖注入配置参数。
     *
     * @param secret JWT签名密钥（从配置文件读取）
     * @param expireHours token过期时间（小时，默认24）
     */
    public JwtUtil(@Value("${app.jwt.secret:eleme-backend-secret}") String secret,
                   @Value("${app.jwt.expire-hours:24}") long expireHours) {
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.expireMillis = expireHours * 3600_000L;
        log.info("JwtUtil初始化完成 - 过期时间: {}小时", expireHours);
    }

    /**
     * 生成token（默认角色为普通用户）。
     *
     * @param userId 用户ID
     * @return JWT token
     */
    public String create(Integer userId) {
        return create(userId, 0);
    }

    /**
     * 生成token（指定角色）。
     *
     * @param userId 用户ID
     * @param role 角色（0=普通用户, 1=商家, 2=管理员）
     * @return JWT token
     */
    public String create(Integer userId, Integer role) {
        try {
            long now = System.currentTimeMillis();
            long iat = now / 1000L;
            long exp = (now + expireMillis) / 1000L;

            // 使用Jackson生成payload JSON
            ObjectNode payload = MAPPER.createObjectNode();
            payload.put("sub", userId);
            payload.put("role", role);
            payload.put("iat", iat);
            payload.put("exp", exp);
            payload.put("jti", UUID.randomUUID().toString());

            String headerB64 = base64Url(HEADER_JSON);
            String payloadB64 = base64Url(MAPPER.writeValueAsString(payload));
            String body = headerB64 + "." + payloadB64;
            String signature = base64Url(sign(body));

            return body + "." + signature;
        } catch (Exception e) {
            throw new IllegalStateException("生成token失败", e);
        }
    }

    /**
     * 从token中快速提取userId（不验证签名，用于日志等非安全场景）。
     *
     * @param token JWT token
     * @return userId，如果解析失败返回null
     */
    public Integer id(String token) {
        try {
            return parse(token).userId();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析并验证token，返回登录用户信息。
     * 
     * 验证步骤：
     * 1. 格式检查（3部分）
     * 2. 签名验证（防篡改）
     * 3. 过期检查（exp字段）
     * 4. 提取userId和role
     *
     * @param token JWT token
     * @return 登录用户信息
     * @throws SecurityException token无效、过期、格式错误、签名错误
     */
    public LoginUser parse(String token) {
        if (token == null || token.isBlank()) {
            throw new SecurityException("token为空");
        }

        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new SecurityException("token格式错误");
            }

            // 1. 签名验证（防篡改）
            String body = parts[0] + "." + parts[1];
            String expectedSignature = base64Url(sign(body));
            if (!MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw new SecurityException("token签名无效");
            }

            // 2. 解析payload（JSON解析）
            String payloadJson = new String(
                    Base64.getUrlDecoder().decode(parts[1]),
                    StandardCharsets.UTF_8);
            JsonNode payload = MAPPER.readTree(payloadJson);

            // 3. 提取字段
            Integer userId = payload.path("sub").asInt();
            if (userId == 0) {
                throw new SecurityException("token缺少userId");
            }

            Integer role = payload.path("role").asInt(0);

            // 4. 过期检查
            long exp = payload.path("exp").asLong(0);
            if (exp > 0) {
                long expMillis = exp * 1000L;
                if (expMillis < System.currentTimeMillis()) {
                    throw new SecurityException("token已过期");
                }
            }

            return new LoginUser(userId, role, token);

        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException("token解析失败", e);
        }
    }

    /**
     * 计算token的SHA-256哈希（用于单会话校验）。
     * 
     * 使用场景：
     * - 登录时：将hash存入user.current_token_hash
     * - 请求时：对比当前token的hash与数据库中的hash
     * - 登出时：清空user.current_token_hash
     * - 实现效果：同一账号在其他地方登录会导致旧token失效
     *
     * @param token JWT token
     * @return Base64编码的SHA-256哈希值
     */
    public String hash(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return base64Url(hashBytes);
        } catch (Exception e) {
            throw new IllegalStateException("计算token哈希失败", e);
        }
    }

    /**
     * 签名（HMAC-SHA256）。
     */
    private byte[] sign(String content) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64URL编码（字符串）。
     */
    private String base64Url(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64URL编码（字节数组）。
     */
    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(bytes);
    }
}
