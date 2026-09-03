package com.iteleme.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户展示对象。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {
    /** 用户编号。 */
    private String id;
    /** 用户名称。 */
    private String name;
    /** 用户性别。 */
    private Integer sex;
    /** 用户头像。 */
    private String avatar;
    /** 删除标记。 */
    private Integer delFlag;
}
