package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("""
            SELECT userId AS id, password, userName AS name, userSex AS sex,
                   userImg AS avatar, delTag AS delFlag
            FROM `user` WHERE userId = #{userId}
            """)
    User findById(@Param("userId") String userId);

    @Select("""
            SELECT userId AS id, password, userName AS name, userSex AS sex,
                   userImg AS avatar, delTag AS delFlag
            FROM `user` WHERE userId = #{userId} AND delTag = 1
            """)
    User findActiveById(@Param("userId") String userId);

    @Insert("""
            INSERT INTO `user`(userId, password, userName, userSex, userImg, delTag)
            VALUES (#{id}, #{password}, #{name}, #{sex}, #{avatar}, 1)
            """)
    int insert(User user);
}
