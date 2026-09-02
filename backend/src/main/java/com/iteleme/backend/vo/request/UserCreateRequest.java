package com.iteleme.backend.vo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateRequest {
    private String userId;
    private String password;
    private String userName;
    private Integer userSex;
    private String userImg;
}
