package com.iteleme.backend.context;

import com.iteleme.backend.constant.UserRole;

public record LoginUser(Integer userId, UserRole role, String token) {
}
