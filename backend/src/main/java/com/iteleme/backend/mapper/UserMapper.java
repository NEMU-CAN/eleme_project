package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户数据访问接口。
 */
@Mapper
public interface UserMapper {
    /**
     * 根据用户编号查询用户（包含删除标记）。
     */
    @Select("""
            SELECT id, password, name, sex, avatar, del_flag
            FROM `user`
            WHERE id = #{userId}
            """)
    User findById(@Param("userId") String userId);

    /**
     * 查询正常状态的用户。
     */
    @Select("""
            SELECT id, password, name, sex, avatar, del_flag
            FROM `user`
            WHERE id = #{userId}
              AND del_flag = 1
            """)
    User findActiveById(@Param("userId") String userId);

    /**
     * 新增用户。
     */
    @Insert("""
            INSERT INTO `user`(id, password, name, sex, avatar, del_flag)
            VALUES (#{id}, #{password}, #{name}, #{sex}, #{avatar}, 1)
            """)
    int insert(User user);
}
