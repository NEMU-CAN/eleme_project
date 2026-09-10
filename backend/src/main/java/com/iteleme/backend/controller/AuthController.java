package com.iteleme.backend.controller;

import com.iteleme.backend.common.Result;
import com.iteleme.backend.dto.LoginRequest;
import com.iteleme.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody @Valid LoginRequest request) {
        return Result.success(userService.login(request.phone(), request.password()));
    }

    @DeleteMapping("/logout")
    public Result logout() {
        userService.logout();
        return Result.success();
    }
}
