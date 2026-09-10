package com.iteleme.backend.service.support;

import com.iteleme.backend.entity.User;
import com.iteleme.backend.exception.ForbiddenException;
import com.iteleme.backend.exception.NotFoundException;
import com.iteleme.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 用户校验器：收拢"用户存在且处于正常状态"的校验（纵深防御）。
 */
@Component
@RequiredArgsConstructor
public class UserValidator {
    private final UserMapper userMapper;

    /**
     * 确认用户存在且处于正常状态（status = 0），否则抛异常。
     *
     * @param userId 用户编号
     * @return 有效用户
     */
    public User requireActive(Integer userId) {
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
