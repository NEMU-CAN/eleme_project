package com.iteleme.backend.service.impl;

import com.iteleme.backend.common.FieldErrorVO;
import com.iteleme.backend.config.JwtUtil;
import com.iteleme.backend.constant.UserRole;
import com.iteleme.backend.context.CurrentUserContext;
import com.iteleme.backend.dto.LoginRequest;
import com.iteleme.backend.dto.UserCreateRequest;
import com.iteleme.backend.dto.UserUpdateRequest;
import com.iteleme.backend.entity.User;
import com.iteleme.backend.exception.ConflictException;
import com.iteleme.backend.exception.ForbiddenException;
import com.iteleme.backend.exception.NotFoundException;
import com.iteleme.backend.exception.UnauthorizedException;
import com.iteleme.backend.mapper.UserMapper;
import com.iteleme.backend.service.UserService;
import com.iteleme.backend.vo.UserVO;
import com.iteleme.backend.support.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserVO register(UserCreateRequest request) {
        if (userMapper.findByPhone(request.phone()) != null) {
            throw new ConflictException("手机号已存在", java.util.List.of(new FieldErrorVO("phone", "手机号已存在")));
        }
        User user = new User(
                userMapper.nextId(),
                request.nickname(),
                request.password(),
                request.phone(),
                request.avatar(),
                request.gender(),
                UserRole.CUSTOMER,
                0,
                null
        );
        userMapper.insert(user);
        return UserVO.from(user);
    }

    @Override
    @Transactional
    public UserVO login(String phone, String password) {
        User user = userMapper.findByPhone(phone);
        if (user == null || !password.equals(user.getPassword())) {
            throw new UnauthorizedException("手机号或密码错误");
        }
        if (user.getStatus() != 0) {
            throw new ForbiddenException("账号已禁用");
        }
        String token = JwtUtil.create(user.getId(), user.getRole());
        user.setCurrentTokenHash(TokenHashUtil.hash(token));
        userMapper.update(user);
        return UserVO.from(user).withToken(token);
    }

    @Override
    public UserVO current() {
        User user = loadCurrentUser();
        return UserVO.from(user);
    }

    @Override
    @Transactional
    public UserVO update(UserUpdateRequest request) {
        User user = loadCurrentUser();
        if (request.phone() != null && !request.phone().equals(user.getPhone())) {
            User exist = userMapper.findByPhone(request.phone());
            if (exist != null && !exist.getId().equals(user.getId())) {
                throw new ConflictException("手机号已存在", java.util.List.of(new FieldErrorVO("phone", "手机号已存在")));
            }
            user.setPhone(request.phone());
        }
        if (request.nickname() != null) {
            user.setNickname(request.nickname());
        }
        if (request.avatar() != null) {
            user.setAvatar(request.avatar());
        }
        if (request.gender() != null) {
            user.setGender(request.gender());
        }
        // 资料变更后主动让本地 token 失效，要求客户端重新登录。
        user.setCurrentTokenHash(null);
        userMapper.update(user);
        return UserVO.from(user);
    }

    @Override
    @Transactional
    public void logout() {
        User user = loadCurrentUser();
        // 单会话模式下，登出就是清空当前有效 token 的哈希。
        user.setCurrentTokenHash(null);
        userMapper.update(user);
    }

    private User loadCurrentUser() {
        Integer userId = CurrentUserContext.userId();
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new NotFoundException("用户不存在");
        }
        if (user.getStatus() != 0) {
            throw new ForbiddenException("账号已禁用");
        }
        return user;
    }
}
