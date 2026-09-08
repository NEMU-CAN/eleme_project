package com.iteleme.backend.service.support;

import com.iteleme.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// ============================================================
// [第一步 新增] token 失效器：注销时使该用户已签发 token 失效
// ============================================================
@Component
@RequiredArgsConstructor
public class TokenInvalidator {
    /** 用户表数据访问对象。 */
    private final UserMapper userMapper;

    /**
     * 使该用户所有已签发 token 失效。
     * <p>[第二步 已实现] 单会话方案：清空 user.current_token_hash（登录时写入的当前 token 哈希），
     * 拦截器按「token 哈希 == current_token_hash」校验 → 旧 token 立即 401。</p>
     */
    public void invalidate(String userId) {
        userMapper.updateCurrentTokenHash(userId, null);
    }
}
