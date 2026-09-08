package com.iteleme.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank(message = "手机号不能为空") @Size(max = 20, message = "手机号长度不能超过20") String phone,
        @NotBlank(message = "密码不能为空") @Size(max = 255, message = "密码长度不能超过255") String password,
        @NotBlank(message = "昵称不能为空") @Size(max = 20, message = "昵称长度不能超过20") String nickname,
        @NotNull(message = "性别不能为空") Integer gender,
        String avatar
) {
}
