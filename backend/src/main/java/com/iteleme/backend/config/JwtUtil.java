package com.iteleme.backend.config;

// ============================================================
// [阶段① 新增] JWT 工具：生成 / 解析签名 token（HS256，无额外依赖）
// 说明：用 JDK 自带 crypto + tools.jackson 拼标准 JWT（header.payload.signature）。
//       阶段②③ 收紧鉴权时可复用；如需换第三方 JWT 库，仅改本类。
// ============================================================
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class JwtUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    private final SecretKeySpec key;
    private final long expireMillis;

    public JwtUtil(@Value("${app.jwt.secret:codex-workS-eleme-secret}") String secret,
                   @Value("${app.jwt.expire-hours:24}") long expireHours) {
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.expireMillis = expireHours * 3600L * 1000L;
    }

    /** 为指定用户生成签名 token。 */
    public String generateToken(String userId) {
        try {
            long now = System.currentTimeMillis();
            ObjectNode payload = MAPPER.createObjectNode();
            payload.put("sub", userId);
            payload.put("iat", now / 1000L);
            payload.put("exp", (now + expireMillis) / 1000L);
            String body = base64Url(HEADER_JSON) + "." + base64Url(MAPPER.writeValueAsString(payload));
            return body + "." + base64Url(sign(body));
        } catch (Exception e) {
            throw new IllegalStateException("生成 token 失败", e);
        }
    }

    /** 解析 token 中的用户编号；无效 / 过期返回 null。 */
    public String parseUserId(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            String body = parts[0] + "." + parts[1];
            String expected = base64Url(sign(body));
            if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8))) {
                return null;
            }
            ObjectNode payload = (ObjectNode) MAPPER.readTree(
                    new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8));
            long exp = payload.path("exp").asLong();
            if (exp > 0 && exp * 1000L < System.currentTimeMillis()) {
                return null;
            }
            return payload.path("sub").asText(null);
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] sign(String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        return mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
    }

    private static String base64Url(String s) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private static String base64Url(byte[] b) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
}
