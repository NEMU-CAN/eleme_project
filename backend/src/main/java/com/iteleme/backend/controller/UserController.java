package com.iteleme.backend.controller;

import com.iteleme.backend.service.UserService;
import com.iteleme.backend.vo.UserVO;
import com.iteleme.backend.vo.request.LoginRequest;
import com.iteleme.backend.vo.request.UserCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/users/{userId}")
    public UserVO getUserById(@PathVariable("userId") String userId) {
        return userService.getUserById(userId);
    }

    @PostMapping("/users")
    public ResponseEntity<UserVO> createUser(@RequestBody UserCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }

    @PostMapping("/sessions")
    public UserVO createSession(@RequestBody LoginRequest request) {
        return userService.login(request);
    }
}
