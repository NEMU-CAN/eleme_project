package com.iteleme.backend.service.impl;

import com.iteleme.backend.entity.User;
import com.iteleme.backend.exception.ResourceNotFoundException;
import com.iteleme.backend.mapper.UserMapper;
import com.iteleme.backend.service.UserService;
import com.iteleme.backend.vo.request.LoginRequest;
import com.iteleme.backend.vo.request.UserCreateRequest;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User getUserById(String userId) {
        requireUserId(userId);
        User user = userMapper.findActiveById(userId);
        if (user == null) throw new ResourceNotFoundException("用户不存在");
        return user;
    }

    @Override
    public User createUser(UserCreateRequest request) {
        if (request == null) throw new IllegalArgumentException("请求体不能为空");
        requireUserId(request.getUserId());
        requirePassword(request.getPassword());
        requireName(request.getUserName());
        requireSex(request.getUserSex());
        if (userMapper.findById(request.getUserId()) != null) {
            throw new IllegalStateException("用户编号已存在");
        }
        User user = new User();
        user.setUserId(request.getUserId());
        user.setPassword(request.getPassword());
        user.setUserName(request.getUserName());
        user.setUserSex(request.getUserSex());
        user.setUserImg(request.getUserImg());
        user.setDelTag(1);
        userMapper.insert(user);
        return user;
    }

    @Override
    public User login(LoginRequest request) {
        if (request == null) throw new IllegalArgumentException("请求体不能为空");
        requireUserId(request.getUserId());
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        User user = userMapper.findById(request.getUserId());
        if (user == null || !Objects.equals(user.getDelTag(), 1)) {
            throw new ResourceNotFoundException("用户不存在");
        }
        if (!Objects.equals(user.getPassword(), request.getPassword())) {
            throw new SecurityException("密码错误");
        }
        user.setPassword(null);
        return user;
    }

    public String token(String userId) {
        return userId + "." + UUID.randomUUID();
    }

    private static void requireUserId(String value) {
        if (value == null || value.isBlank() || value.length() > 20) throw new IllegalArgumentException("userId 无效");
    }
    private static void requirePassword(String value) {
        if (value == null || value.length() < 8 || value.length() > 128) throw new IllegalArgumentException("password 无效");
    }
    private static void requireName(String value) {
        if (value == null || value.isBlank() || value.length() > 20) throw new IllegalArgumentException("userName 无效");
    }
    private static void requireSex(Integer value) {
        if (value == null || (value != 0 && value != 1)) throw new IllegalArgumentException("userSex 无效");
    }
}
