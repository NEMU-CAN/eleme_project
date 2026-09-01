package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("""
            SELECT userId, password, userName, userSex, userImg, delTag
            FROM `user`
            WHERE userId = #{userId}
            """)
    User findById(@Param("userId") String userId);

    @Select("""
            SELECT userId, password, userName, userSex, userImg, delTag
            FROM `user`
            WHERE userId = #{userId}
              AND delTag = 1
            """)
    User findActiveById(@Param("userId") String userId);

    @Insert("""
            INSERT INTO `user`(userId, password, userName, userSex, userImg, delTag)
            VALUES (#{userId}, #{password}, #{userName}, #{userSex}, #{userImg}, 1)
            """)
    int insert(User user);
}
