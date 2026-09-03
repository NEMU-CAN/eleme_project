package com.iteleme.backend.vo.request;

import lombok.Data;

@Data
public class UserCreateRequest {
    private String userId;
    private String password;
    private String userName;
    private Integer userSex;
    private String userImg;
}
