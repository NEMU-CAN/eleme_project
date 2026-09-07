package com.iteleme.backend.service.impl;

import com.iteleme.backend.exception.ApiException;
import com.iteleme.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// ============================================================
// [重构] 用户校验器：收拢"用户存在且处于正常状态"的校验
// 说明：受保护接口的 AuthInterceptor 已校验用户状态，此处作为服务层兜底（防御纵深），
//       避免业务服务各自重复实现同一段校验逻辑。
// ============================================================
@Component
@RequiredArgsConstructor
public class UserValidator {
    /** 用户表数据访问对象。 */
    private final UserMapper userMapper;

    /**
     * 确认用户存在且处于正常状态（del_flag = 1），否则抛 404。
     */
    public void requireActive(String userId) {
        ServiceValidator.requireUserId(userId);
        if (userMapper.findActiveById(userId) == null) {
            throw ApiException.notFound();
        }
    }
}
