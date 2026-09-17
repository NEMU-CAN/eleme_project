package com.iteleme.backend.dto;

import com.iteleme.backend.constant.GenderType;
import com.iteleme.backend.constant.UserRole;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record AdminUserUpdateRequest(
        @Size(max = 20, message = "昵称长度不能超过20") String nickname,
        @Size(max = 20, message = "手机号长度不能超过20") String phone,
        String avatar,
        GenderType gender,
        UserRole role,
        @Min(value = -1, message = "用户状态无效") @Max(value = 0, message = "用户状态无效") Integer status
) {
}
