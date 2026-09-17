package com.iteleme.backend.mapper;

import com.iteleme.backend.constant.UserRole;
import com.iteleme.backend.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {
    User findById(@Param("id") Integer id);

    User findByPhone(@Param("phone") String phone);

    List<User> list(@Param("keyword") String keyword,
                    @Param("role") UserRole role,
                    @Param("status") Integer status);

    int insert(User user);

    int update(User user);

    Integer nextId();
}
