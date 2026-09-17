package com.iteleme.backend.service;

import com.iteleme.backend.constant.UserRole;
import com.iteleme.backend.dto.AdminUserUpdateRequest;
import com.iteleme.backend.dto.UserCreateRequest;
import com.iteleme.backend.dto.UserUpdateRequest;
import com.iteleme.backend.vo.UserVO;

import java.util.List;

public interface UserService {
    UserVO register(UserCreateRequest request);

    UserVO login(String phone, String password);

    UserVO current();

    UserVO update(UserUpdateRequest request);

    List<UserVO> listForAdmin(String keyword, UserRole role, Integer status);

    UserVO updateForAdmin(Integer id, AdminUserUpdateRequest request);

    void logout();

    void deleteAccount();
}
