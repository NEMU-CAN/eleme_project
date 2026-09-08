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
            SELECT id, password, name, sex, avatar, del_flag AS delFlag, current_token_hash AS currentTokenHash
            FROM `user`
            WHERE id = #{userId}
            """)
    User findById(@Param("userId") String userId);

    @Select("""
            SELECT id, password, name, sex, avatar, del_flag AS delFlag, current_token_hash AS currentTokenHash
            FROM `user`
            WHERE id = #{userId} AND del_flag = 1
            """)
    User findActiveById(@Param("userId") String userId);

    @Insert("""
            INSERT INTO `user`(id, password, name, sex, avatar, del_flag)
            VALUES (#{id}, #{password}, #{name}, #{sex}, #{avatar}, 1)
            """)
    int insert(User user);

    // ===== [第二步 新增] 当前有效 token 哈希：登录写入、注销清空（单会话） =====
    /** 写入/清空当前有效 token 哈希（tokenHash 为 null 即清空），仅对正常用户生效。 */
    @Update("""
            UPDATE `user`
            SET current_token_hash = #{tokenHash}
            WHERE id = #{userId} AND del_flag = 1
            """)
    int updateCurrentTokenHash(@Param("userId") String userId, @Param("tokenHash") String tokenHash);
    // ===== [第二步 新增结束] =====

    // ===== [第一步 新增] 删除账户（软删）与恢复（同 userId 重新注册） =====
    /** 将用户标记为已删除（软删），仅对正常状态用户生效；同时清空 token 哈希（防重注册后旧 token 复活）。 */
    @Update("""
            UPDATE `user`
            SET del_flag = 0,
                current_token_hash = NULL
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
