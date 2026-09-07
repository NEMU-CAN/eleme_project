package com.iteleme.backend.config;

// ============================================================
// [阶段① 新增] 鉴权拦截器
// 说明：解析 Authorization: Bearer <token>，把 token 里的 userId 覆盖进路径变量 Map
//       （HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE），这样控制器 @PathVariable String userId
//       自动拿到 token 身份（token 优先），鉴权逻辑收拢到此处，控制器无需感知 token。
//       阶段①为兼容模式：无有效 token 时不拦截，回落路径 userId，仅打一条警告日志。
//       阶段②③ 收紧时，把 return true 的分支改为抛 401 即可。
// ============================================================
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    // public AuthInterceptor(JwtUtil jwtUtil) {
    //     this.jwtUtil = jwtUtil;
    // }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = null;
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            userId = jwtUtil.parseUserId(auth.substring(7));
        }
        if (userId != null) {
            // [阶段①] token 优先：覆盖路径变量 userId，控制器 @PathVariable userId 自动拿到 token 身份
            Map<String, String> uriVars = (Map<String, String>) request.getAttribute(
                    HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            if (uriVars != null && uriVars.containsKey("userId")) {
                uriVars.put("userId", userId);
            }
        } else {
            // 阶段①兼容：未带有效 token，回落路径 userId（前端带 token 前不拦截）
            System.out.println("[AuthInterceptor][deprecated] 请求未带有效 token，回落路径 userId: "
                    + request.getRequestURI());
        }
        return true;
    }
}
