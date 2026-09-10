package com.iteleme.backend.config;

import com.iteleme.backend.context.CurrentUserContext;
import com.iteleme.backend.context.LoginUser;
import com.iteleme.backend.entity.User;
import com.iteleme.backend.exception.UnauthorizedException;
import com.iteleme.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String method = request.getMethod();
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (isPublicRoute(method, path)) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new UnauthorizedException("请先登录");
        }

        String token = authorization.substring(7);
        LoginUser currentUser;
        try {
            currentUser = jwtUtil.parse(token);
        } catch (RuntimeException e) {
            throw new UnauthorizedException("登录已失效，请重新登录");
        }
        User user = userMapper.findById(currentUser.userId());
        if (user == null || user.getStatus() != 0) {
            throw new UnauthorizedException("账号不可用");
        }
        String currentTokenHash = jwtUtil.hash(token);
        if (!java.util.Objects.equals(currentTokenHash, user.getCurrentTokenHash())) {
            throw new UnauthorizedException("登录已失效，请重新登录");
        }
        currentUser = new LoginUser(user.getId(), user.getRole(), token);
        CurrentUserContext.set(currentUser);
        request.setAttribute("currentUserId", currentUser.userId());
        request.setAttribute("currentUserRole", currentUser.role());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        CurrentUserContext.clear();
    }

    private boolean isPublicRoute(String method, String path) {
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        return ("POST".equalsIgnoreCase(method) && "/users".equals(path))
                || ("POST".equalsIgnoreCase(method) && "/login".equals(path))
                || ("GET".equalsIgnoreCase(method) && (path.equals("/businesses") || path.startsWith("/businesses/")
                || path.equals("/foods") || path.startsWith("/foods/") || path.equals("/tastes")));
    }
}
