package com.iteleme.backend.service.support;

import com.iteleme.backend.entity.User;
import com.iteleme.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * token 失效器：使该用户已签发的 token 失效。
 */
@Component
@RequiredArgsConstructor
public class TokenInvalidator {
    private final UserMapper userMapper;

    /**
     * 单会话方案：清空 user.current_token_hash，拦截器按「token 哈希 == current_token_hash」校验 → 旧 token 立即 401。
     *
     * @param userId 用户编号
     */
    public void invalidate(Integer userId) {
        User user = userMapper.findById(userId);
        if (user != null) {
            user.setCurrentTokenHash(null);
            userMapper.update(user);
        }
    }
}
