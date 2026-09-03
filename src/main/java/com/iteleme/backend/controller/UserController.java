package com.iteleme.backend.controller;

import com.iteleme.backend.entity.User;
import com.iteleme.backend.service.UserService;
import com.iteleme.backend.vo.request.LoginRequest;
import com.iteleme.backend.vo.request.UserCreateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PostMapping("/sessions")
    public ResponseEntity<Map<String, Object>> createSession(@RequestBody LoginRequest request) {
        User user = userService.login(request);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userId", user.getUserId());
        response.put("userName", user.getUserName());
        response.put("userSex", user.getUserSex());
        response.put("userImg", user.getUserImg());
        response.put("delTag", user.getDelTag());
        response.put("token", user.getUserId() + ".session");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserById(@org.springframework.web.bind.annotation.PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/users/me")
    public ResponseEntity<User> getCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        String resolvedUserId = userId;
        if ((resolvedUserId == null || resolvedUserId.isBlank()) && authorization != null
                && authorization.startsWith("Bearer ")) {
            String token = authorization.substring("Bearer ".length());
            int separator = token.indexOf('.');
            resolvedUserId = separator > 0 ? token.substring(0, separator) : token;
        }
        if (resolvedUserId == null || resolvedUserId.isBlank()) {
            throw new IllegalArgumentException("缺少登录凭证");
        }
        return ResponseEntity.ok(userService.getUserById(resolvedUserId));
    }
}
