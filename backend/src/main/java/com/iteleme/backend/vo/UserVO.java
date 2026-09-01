package com.iteleme.backend.vo;

import lombok.Data;

@Data
public class UserVO {
    private String userId;
    private String userName;
    private Integer userSex;
    private String userImg;
    private Integer delTag;
}
