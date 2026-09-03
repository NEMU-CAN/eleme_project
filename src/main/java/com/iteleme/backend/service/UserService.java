package com.iteleme.backend.service;

import com.iteleme.backend.entity.User;
import com.iteleme.backend.vo.request.LoginRequest;
import com.iteleme.backend.vo.request.UserCreateRequest;

public interface UserService {
    User getUserById(String userId);
    User createUser(UserCreateRequest request);
    User login(LoginRequest request);
}
