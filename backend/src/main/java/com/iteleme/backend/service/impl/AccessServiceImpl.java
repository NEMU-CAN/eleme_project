package com.iteleme.backend.service.impl;

import com.iteleme.backend.constant.UserRole;
import com.iteleme.backend.context.CurrentUserContext;
import com.iteleme.backend.exception.ForbiddenException;
import com.iteleme.backend.mapper.BusinessAdminMapper;
import com.iteleme.backend.service.AccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessServiceImpl implements AccessService {
    private final BusinessAdminMapper businessAdminMapper;

    @Override
    public void ensureBusinessOwner(Integer businessId) {
        Integer role = CurrentUserContext.role();
        if (role != UserRole.BUSINESS && role != UserRole.ADMIN) {
            throw new ForbiddenException("无权限操作");
        }
        if (role == UserRole.ADMIN) {
            return;
        }
        Integer userId = CurrentUserContext.userId();
        if (businessAdminMapper.findByUserIdAndBusinessId(userId, businessId) == null) {
            throw new ForbiddenException("无权管理该商家");
        }
    }

    @Override
    public List<Integer> ownedBusinessIds(Integer userId) {
        return businessAdminMapper.listBusinessIdsByUserId(userId);
    }
}
