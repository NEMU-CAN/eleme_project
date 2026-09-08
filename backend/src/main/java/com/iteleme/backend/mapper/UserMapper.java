package com.iteleme.backend.mapper;

import com.iteleme.backend.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    // ===== [第一步 新增] 删除账户（软删）与恢复（同 userId 重新注册） =====
    /** 将用户标记为已删除（软删），仅对正常状态用户生效。 */
    @Update("""
            UPDATE `user`
            SET del_flag = 0
            WHERE id = #{userId} AND del_flag = 1
            """)
    int markAsDeleted(@Param("userId") String userId);

    /** 恢复已删除用户（同 userId 重新注册），仅对已删除用户生效。 */
    @Update("""
            UPDATE `user`
            SET password = #{password},
                name = #{name},
                sex = #{sex},
                avatar = #{avatar},
                del_flag = 1
            WHERE id = #{id} AND del_flag = 0
            """)
    int reactivate(User user);
    // ===== [第一步 新增结束] =====
}
