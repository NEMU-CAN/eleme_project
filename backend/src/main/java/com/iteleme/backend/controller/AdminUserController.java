package com.iteleme.backend.controller;

import com.iteleme.backend.common.Result;
import com.iteleme.backend.constant.UserRole;
import com.iteleme.backend.dto.AdminUserUpdateRequest;
import com.iteleme.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserService userService;

    @GetMapping
    public Result list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) UserRole role,
                       @RequestParam(required = false) Integer status) {
        return Result.success(userService.listForAdmin(keyword, role, status));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Integer id,
                         @RequestBody @Valid AdminUserUpdateRequest request) {
        return Result.success(userService.updateForAdmin(id, request));
    }
}
