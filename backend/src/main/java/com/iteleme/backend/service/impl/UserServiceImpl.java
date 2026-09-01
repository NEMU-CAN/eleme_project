package com.iteleme.backend.service.impl;

import com.iteleme.backend.entity.User;
import com.iteleme.backend.exception.ApiException;
import com.iteleme.backend.mapper.UserMapper;
import com.iteleme.backend.service.UserService;
import com.iteleme.backend.vo.UserVO;
import com.iteleme.backend.vo.request.LoginRequest;
import com.iteleme.backend.vo.request.UserCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    @Override
    public UserVO getUserById(String userId) {
        ServiceValidator.requireUserId(userId);
        User user = userMapper.findActiveById(userId);
        if (user == null) {
            throw ApiException.notFound();
        }
        return VoConverters.toUserVO(user);
    }

    @Override
    public UserVO createUser(UserCreateRequest request) {
        validateCreateRequest(request);
        if (userMapper.findById(request.getUserId()) != null) {
            throw ApiException.conflict("userId", "用户编号已存在");
        }

        User user = new User();
        user.setUserId(request.getUserId());
        user.setPassword(request.getPassword());
        user.setUserName(request.getUserName());
        user.setUserSex(request.getUserSex());
        user.setUserImg(request.getUserImg());
        user.setDelTag(1);
        userMapper.insert(user);
        return VoConverters.toUserVO(user);
    }

    @Override
    public UserVO login(LoginRequest request) {
        if (request == null) {
            throw ApiException.badRequest("body", "请求体不能为空");
        }
        ServiceValidator.requireUserId(request.getUserId());
        ServiceValidator.requireNonBlank(request.getPassword(), "password");
        ServiceValidator.requireMaxLength(request.getPassword(), "password", 20);

        User user = userMapper.findById(request.getUserId());
        if (user == null || !Objects.equals(user.getDelTag(), 1)) {
            throw ApiException.notFound();
        }
        if (!Objects.equals(user.getPassword(), request.getPassword())) {
            throw ApiException.unauthorized();
        }
        return VoConverters.toUserVO(user);
    }

    private void validateCreateRequest(UserCreateRequest request) {
        if (request == null) {
            throw ApiException.badRequest("body", "请求体不能为空");
        }
        ServiceValidator.requireUserId(request.getUserId());
        ServiceValidator.requireNonBlank(request.getPassword(), "password");
        ServiceValidator.requireMaxLength(request.getPassword(), "password", 20);
        ServiceValidator.requireNonBlank(request.getUserName(), "userName");
        ServiceValidator.requireMaxLength(request.getUserName(), "userName", 20);
        ServiceValidator.requireZeroOrOne(request.getUserSex(), "userSex");
    }
}
