package com.iteleme.backend.controller;

import com.iteleme.backend.service.UserService;
import com.iteleme.backend.vo.UserVO;
import com.iteleme.backend.vo.request.LoginRequest;
import com.iteleme.backend.vo.request.UserCreateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** 用户注册、登录及资料接口。 */
@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(toResponse(userService.getUserById(userId)));
    }

    @GetMapping("/users/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ResponseEntity.ok(toResponse(userService.getUserById(resolveUserId(userId, authorization))));
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(userService.createUser(request)));
    }

    @PostMapping("/sessions")
    public ResponseEntity<Map<String, Object>> createSession(@RequestBody LoginRequest request) {
        UserVO user = userService.login(request);
        Map<String, Object> response = toResponse(user);
        response.put("token", user.getId() + "." + UUID.randomUUID());
        return ResponseEntity.ok(response);
    }

    private static Map<String, Object> toResponse(UserVO user) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userId", user.getId());
        response.put("userName", user.getName());
        response.put("userSex", user.getSex());
        response.put("userImg", user.getAvatar());
        response.put("delTag", user.getDelFlag());
        return response;
    }

    private static String resolveUserId(String userId, String authorization) {
        if (userId != null && !userId.isBlank()) return userId;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            int separator = token.indexOf('.');
            return separator > 0 ? token.substring(0, separator) : token;
        }
        throw com.iteleme.backend.exception.ApiException.badRequest("Authorization", "缺少登录凭证");
    }
}
