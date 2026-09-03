package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("""
            SELECT id, password, name, sex, avatar, del_flag AS delFlag
            FROM `user`
            WHERE id = #{userId}
            """)
    User findById(@Param("userId") String userId);

    @Select("""
            SELECT id, password, name, sex, avatar, del_flag AS delFlag
            FROM `user`
            WHERE id = #{userId} AND del_flag = 1
            """)
    User findActiveById(@Param("userId") String userId);

    @Insert("""
            INSERT INTO `user`(id, password, name, sex, avatar, del_flag)
            VALUES (#{id}, #{password}, #{name}, #{sex}, #{avatar}, 1)
            """)
    int insert(User user);
}
