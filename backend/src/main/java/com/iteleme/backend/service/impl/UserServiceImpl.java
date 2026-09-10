package com.iteleme.backend.service.impl;

import com.iteleme.backend.common.FieldErrorVO;
import com.iteleme.backend.config.JwtUtil;
import com.iteleme.backend.constant.UserRole;
import com.iteleme.backend.context.CurrentUserContext;
import com.iteleme.backend.dto.UserCreateRequest;
import com.iteleme.backend.dto.UserUpdateRequest;
import com.iteleme.backend.entity.User;
import com.iteleme.backend.exception.ConflictException;
import com.iteleme.backend.exception.ForbiddenException;
import com.iteleme.backend.exception.UnauthorizedException;
import com.iteleme.backend.mapper.UserMapper;
import com.iteleme.backend.service.UserService;
import com.iteleme.backend.service.support.TokenInvalidator;
import com.iteleme.backend.service.support.UserValidator;
import com.iteleme.backend.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户业务实现。
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final UserValidator userValidator;
    private final TokenInvalidator tokenInvalidator;

    /**
     * 注册新用户。
     */
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

    /**
     * 登录：校验手机号/密码，签发 token 并写入单会话哈希。
     */
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
        String token = jwtUtil.create(user.getId(), user.getRole());
        user.setCurrentTokenHash(jwtUtil.hash(token));
        userMapper.update(user);
        return UserVO.from(user).withToken(token);
    }

    /**
     * 查询当前登录用户资料。
     */
    @Override
    public UserVO current() {
        return UserVO.from(loadCurrentUser());
    }

    /**
     * 更新当前用户资料；资料变更后清空 token 哈希，要求客户端重新登录。
     */
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
        user.setCurrentTokenHash(null);
        userMapper.update(user);
        return UserVO.from(user);
    }

    /**
     * 退出登录：结束当前用户会话，使 token 失效。
     */
    @Override
    @Transactional
    public void logout() {
        User user = loadCurrentUser();
        tokenInvalidator.invalidate(user.getId());
    }

    /**
     * 删除账户（软删）：status 置为 -1，清空 token，所有会话立即失效。
     */
    @Override
    @Transactional
    public void deleteAccount() {
        User user = loadCurrentUser();
        user.setStatus(-1);
        user.setCurrentTokenHash(null);
        userMapper.update(user);
    }

    private User loadCurrentUser() {
        return userValidator.requireActive(CurrentUserContext.userId());
    }
}
