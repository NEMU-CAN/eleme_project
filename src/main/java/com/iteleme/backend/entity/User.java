package com.iteleme.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/** 用户实体，对应 user 表。 */
@Data
public class User {
    private String userId;

    @JsonIgnore
    private String password;

    private String userName;
    private Integer userSex;
    private String userImg;
    private Integer delTag;
}
