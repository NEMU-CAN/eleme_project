package com.iteleme.backend.dto;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(max = 20, message = "昵称长度不能超过20") String nickname,
        @Size(max = 20, message = "手机号长度不能超过20") String phone,
        String avatar,
        Integer gender
) {
}
