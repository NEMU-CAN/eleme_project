package com.iteleme.backend.config;

import com.iteleme.backend.context.LoginUser;
import lombok.SneakyThrows;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JwtUtil {
    private static final String KEY = "eleme-backend-secret";
    private static final Pattern SUB_PATTERN = Pattern.compile("\"sub\"\\s*:\\s*\"?(\\d+)\"?");
    private static final Pattern ROLE_PATTERN = Pattern.compile("\"role\"\\s*:\\s*(-?\\d+)");

    private JwtUtil() {
    }

    public static String create(Integer userId) {
        return create(userId, 0);
    }

    public static String create(Integer userId, Integer role) {
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode("{\"sub\":\"" + userId + "\",\"role\":" + role + ",\"iat\":" + Instant.now().getEpochSecond() + "}");
        return header + "." + payload + "." + sign(header + "." + payload);
    }

    public static Integer id(String token) {
        return parse(token).userId();
    }

    public static LoginUser parse(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("token 格式错误");
            }
            String signed = parts[0] + "." + parts[1];
            if (!sign(signed).equals(parts[2])) {
                throw new SecurityException("无效令牌");
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Integer userId = extractInt(payload, SUB_PATTERN);
            Integer role = extractInt(payload, ROLE_PATTERN);
            if (role == null) {
                role = 0;
            }
            return new LoginUser(userId, role, token);
        } catch (Exception e) {
            throw new SecurityException("无效令牌", e);
        }
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    @SneakyThrows
    private static String sign(String content) {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }

    private static Integer extractInt(String payload, Pattern pattern) {
        Matcher matcher = pattern.matcher(payload);
        if (matcher.find()) {
            return Integer.valueOf(matcher.group(1));
        }
        return null;
    }
}
