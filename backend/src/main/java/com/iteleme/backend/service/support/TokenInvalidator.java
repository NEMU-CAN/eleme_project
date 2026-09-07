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
     * <p>[第二步] 实现：token 版本号（user.token_version 递增）或黑名单，使旧 token 立即失效。</p>
     */
    public void invalidate(String userId) {
        // TODO [第二步] 接入失效机制（token 版本号 / 黑名单），使该用户所有旧 token 立即失效
    }
}
