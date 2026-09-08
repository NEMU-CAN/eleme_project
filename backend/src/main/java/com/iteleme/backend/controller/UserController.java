package com.iteleme.backend.controller;

import com.iteleme.backend.common.Result;
import com.iteleme.backend.service.UserService;
import com.iteleme.backend.vo.request.LoginRequest;
import com.iteleme.backend.vo.request.UserCreateRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 用户注册、登录及资料接口。 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // public UserController(UserService userService) {
    //     this.userService = userService;
    // }

    /** 根据用户编号查询用户资料。 */
    @GetMapping("/users/{userId}")
    public Result getUserById(@PathVariable String userId) {
        return Result.success(userService.getUserById(userId));
    }

    /** 创建新用户。 */
    @PostMapping("/users")
    public ResponseEntity<Result> createUser(@RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success(userService.createUser(request)));
    }

    /** 根据用户编号和密码创建登录会话。 */
    @PostMapping("/sessions")
    public Result createSession(@RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    // ===== [第一步 新增] 退出登录（注销会话） =====
    /** 退出登录：结束当前用户的登录会话。 */
    @DeleteMapping("/users/{userId}/sessions")
    public Result logout(@PathVariable String userId) {
        userService.logout(userId);
        return Result.success();
    }
    // ===== [第一步 新增结束] =====

    // ===== [第一步 新增] 删除账户（软删） =====
    /** 删除账户：软删（del_flag=0），该用户所有 token 立即失效。 */
    @DeleteMapping("/users/{userId}")
    public Result deleteAccount(@PathVariable String userId) {
        userService.deleteAccount(userId);
        return Result.success();
    }
    // ===== [第一步 新增结束] =====
}
