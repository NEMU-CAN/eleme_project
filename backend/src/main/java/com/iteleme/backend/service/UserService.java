package com.iteleme.backend.service;

import com.iteleme.backend.dto.UserCreateRequest;
import com.iteleme.backend.dto.UserUpdateRequest;
import com.iteleme.backend.vo.UserVO;

public interface UserService {
    UserVO register(UserCreateRequest request);

    UserVO login(String phone, String password);

    UserVO current();

    UserVO update(UserUpdateRequest request);

    void logout();
}
