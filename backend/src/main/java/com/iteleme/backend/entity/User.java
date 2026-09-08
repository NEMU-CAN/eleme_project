package com.iteleme.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户实体，对应 `user` 表。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    /** 用户编号。 */
    private String id;
    /** 登录密码。 */
    private String password;
    /** 用户名称。 */
    private String name;
    /** 用户性别。 */
    private Integer sex;
    /** 用户头像。 */
    private String avatar;
    /** 删除标记。 */
    private Integer delFlag;
    // ===== [第二步 新增] 当前有效 token 的哈希（登录写入，注销/删除清空） =====
    /** 当前有效 token 的哈希（NULL 表示当前无有效 token）。 */
    private String currentTokenHash;
    // ===== [第二步 新增结束] =====
}
