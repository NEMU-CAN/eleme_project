package com.iteleme.backend.service;

import com.iteleme.backend.vo.UserVO;
import com.iteleme.backend.vo.request.LoginRequest;
import com.iteleme.backend.vo.request.UserCreateRequest;

public interface UserService {
    UserVO getUserById(String userId);

    UserVO createUser(UserCreateRequest request);

    UserVO login(LoginRequest request);
}
