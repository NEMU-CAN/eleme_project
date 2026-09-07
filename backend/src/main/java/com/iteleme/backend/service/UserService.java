package com.iteleme.backend.service;

import com.iteleme.backend.vo.LoginVO;
import com.iteleme.backend.vo.UserVO;
import com.iteleme.backend.vo.request.LoginRequest;
import com.iteleme.backend.vo.request.UserCreateRequest;

public interface UserService {
    UserVO getUserById(String userId);

    UserVO createUser(UserCreateRequest request);

    // ===== [阶段① 新增] 登录返回 token + 用户信息 =====
    LoginVO login(LoginRequest request);
    // ===== [阶段① 新增结束] =====
}
