package com.iteleme.backend.config;

// ============================================================
// [阶段② 新增] 鉴权拦截器（强制模式）
// 说明：解析 Authorization: Bearer <token>，把 token 里的 userId 覆盖进路径变量 Map
//       （HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE），这样控制器 @PathVariable String userId
//       自动拿到 token 身份（token 优先），鉴权逻辑收拢到此处，控制器无需感知 token。
//       阶段②为强制模式：无有效 token 时直接写 401 响应并 return false（拦截器异常不会被
//       @ControllerAdvice 的 @ExceptionHandler 接住，只能自行写响应）。
//       OPTIONS 预检请求（CORS）不鉴权，直接放行。
// ============================================================
import com.iteleme.backend.common.Result;
import com.iteleme.backend.entity.User;
import com.iteleme.backend.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // CORS 预检请求（OPTIONS）不鉴权，直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String userId = null;
        String token = null;
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring(7);
            userId = jwtUtil.parseUserId(token);
        }
        if (userId == null) {
            // [阶段②] 强制鉴权：无有效 token → 401（拦截器异常不走 @ControllerAdvice，直接写响应）
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(MAPPER.writeValueAsString(Result.error(40101, "未登录或登录已过期")));
            return false;
        }
        // [阶段②] 用户状态校验：用户不存在或已删除（del_flag != 1）→ 视为未登录
        User user = userMapper.findActiveById(userId);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(MAPPER.writeValueAsString(Result.error(40101, "未登录或登录已过期")));
            return false;
        }
        // ===== [第二步 新增] 单会话校验：token 哈希必须等于当前有效 token 哈希 =====
        // 登录时写入 current_token_hash；注销/删除时清空 → 旧 token 立即失效（无需等待过期）。
        if (!Objects.equals(user.getCurrentTokenHash(), jwtUtil.hashToken(token))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(MAPPER.writeValueAsString(Result.error(40101, "未登录或登录已过期")));
            return false;
        }
        // ===== [第二步 新增结束] =====
        // [阶段②] token 优先：把 token 里的 userId 覆盖进路径变量 Map（该 Map 不可变，需复制后替换）
        Map<String, String> uriVars = (Map<String, String>) request.getAttribute(
                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (uriVars != null && uriVars.containsKey("userId")) {
            Map<String, String> copy = new HashMap<>(uriVars);
            copy.put("userId", userId);
            request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, copy);
        }
        return true;
    }
}
