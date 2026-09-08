package com.iteleme.backend.vo;

import com.iteleme.backend.entity.User;

public record UserVO(
        Integer id,
        String nickname,
        String phone,
        String avatar,
        Integer gender,
        Integer role,
        Integer status,
        String token
) {
    public static UserVO from(User user) {
        return new UserVO(user.getId(), user.getNickname(), user.getPhone(), user.getAvatar(), user.getGender(), user.getRole(), user.getStatus(), null);
    }

    public UserVO withToken(String token) {
        return new UserVO(id, nickname, phone, avatar, gender, role, status, token);
    }
}
