package com.iteleme.backend.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UserUpdateRequest(
        @Size(max = 20, message = "昵称长度不能超过20") String nickname,
        @Size(max = 20, message = "手机号长度不能超过20") String phone,
        String avatar,
        @Min(value = 0, message = "性别不能小于0") @Max(value = 2, message = "性别不能大于2")
        Integer gender
) {
}
