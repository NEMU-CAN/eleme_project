package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    User findById(@Param("id") Integer id);

    User findByPhone(@Param("phone") String phone);

    int insert(User user);

    int update(User user);

    Integer nextId();
}
