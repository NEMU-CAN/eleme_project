package com.iteleme.backend.context;

public record LoginUser(Integer userId, Integer role, String token) {
}
