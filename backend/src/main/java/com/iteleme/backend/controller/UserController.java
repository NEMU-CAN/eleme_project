package com.iteleme.backend.controller;

import com.iteleme.backend.common.Result;
import com.iteleme.backend.dto.UserCreateRequest;
import com.iteleme.backend.dto.UserUpdateRequest;
import com.iteleme.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口：注册、查询当前用户、更新资料、删除账户。
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    /**
     * 注册新用户。
     *
     * @param request 注册请求
     * @return 201 + 用户信息
     */
    @PostMapping
    public ResponseEntity<Result> register(@RequestBody @Valid UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(userService.register(request)));
    }

    /**
     * 查询当前登录用户资料。
     *
     * @return 当前用户信息
     */
    @GetMapping
    public Result current() {
        return Result.success(userService.current());
    }

    /**
     * 更新当前用户资料。
     *
     * @param request 更新请求
     * @return 更新后的用户信息
     */
    @PutMapping
    public Result update(@RequestBody @Valid UserUpdateRequest request) {
        return Result.success(userService.update(request));
    }

    /**
     * 删除账户（软删）：status 置为 -1，清空 token，所有会话立即失效。
     *
     * @return 成功响应
     */
    @DeleteMapping
    public Result deleteAccount() {
        userService.deleteAccount();
        return Result.success();
    }
}
