package com.iteleme.backend.service.impl;

import com.iteleme.backend.common.ServiceValidator;
import com.iteleme.backend.common.VoConverters;
import com.iteleme.backend.entity.User;
import com.iteleme.backend.exception.ApiException;
import com.iteleme.backend.mapper.UserMapper;
import com.iteleme.backend.service.UserService;
import com.iteleme.backend.vo.LoginVO;
import com.iteleme.backend.vo.UserVO;
import com.iteleme.backend.vo.request.LoginRequest;
import com.iteleme.backend.vo.request.UserCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

// ===== [阶段① 新增] 注入 JwtUtil 用于生成登录 token =====
import com.iteleme.backend.config.JwtUtil;
// ===== [阶段① 新增结束] =====

@Service
/**
 * 用户业务实现。
 */
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    /** 用户表数据访问对象。 */
    private final UserMapper userMapper;
    // ===== [阶段① 新增] JWT token 工具 =====
    private final JwtUtil jwtUtil;
    // ===== [阶段① 新增结束] =====

    /**
     * 查询用户信息。
     */
    @Override
    public UserVO getUserById(String userId) {
        ServiceValidator.requireUserId(userId);
        User user = userMapper.findActiveById(userId);
        if (user == null) {
            throw ApiException.notFound();
        }
        return VoConverters.toUserVO(user);
    }

    /**
     * 注册用户。
     */
    @Override
    public UserVO createUser(UserCreateRequest request) {
        validateCreateRequest(request);
        if (userMapper.findById(request.getUserId()) != null) {
            throw ApiException.conflict("userId", "用户编号已存在");
        }

        User user = new User();
        user.setId(request.getUserId());
        user.setPassword(request.getPassword());
        user.setName(request.getUserName());
        user.setSex(request.getUserSex());
        user.setAvatar(request.getUserImg());
        user.setDelFlag(1);
        userMapper.insert(user);
        return VoConverters.toUserVO(user);
    }

    /**
     * 用户登录。
     */
    @Override
    public LoginVO login(LoginRequest request) {
        if (request == null) {
            throw ApiException.badRequest("body", "请求体不能为空");
        }
        ServiceValidator.requireUserId(request.getUserId());
        ServiceValidator.requireNonBlank(request.getPassword(), "password");
        ServiceValidator.requireMaxLength(request.getPassword(), "password", 20);

        User user = userMapper.findById(request.getUserId());
        if (user == null || !Objects.equals(user.getDelFlag(), 1)) {
            throw ApiException.notFound();
        }
        if (!Objects.equals(user.getPassword(), request.getPassword())) {
            throw ApiException.unauthorized();
        }
        // ===== [阶段① 新增] 登录成功生成 token =====
        String token = jwtUtil.generateToken(user.getId());
        return new LoginVO(token, VoConverters.toUserVO(user));
        // ===== [阶段① 新增结束] =====
    }

    // ===== [第一步 新增] 退出登录（注销会话） =====
    /**
     * 退出登录：结束当前用户的登录会话。
     */
    @Override
    public void logout(String userId) {
        ServiceValidator.requireUserId(userId);
        if (userMapper.findActiveById(userId) == null) {
            throw ApiException.notFound();
        }
        // 失效机制留待第二步（token 鉴权问题）：当前仅保证框架可跑，注销接口已就位
        invalidateTokens(userId);
    }

    /**
     * 使该用户已签发的 token 失效。
     * <p>[第二步] 实现：token 版本号（user.token_version 递增）或黑名单，使旧 token 立即失效。</p>
     */
    private void invalidateTokens(String userId) {
        // TODO [第二步] 接入失效机制（token 版本号 / 黑名单），使该用户所有旧 token 立即失效
    }
    // ===== [第一步 新增结束] =====

    /**
     * 校验注册请求体。
     */
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
