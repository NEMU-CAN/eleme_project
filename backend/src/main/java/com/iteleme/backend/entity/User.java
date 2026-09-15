package com.iteleme.backend.entity;

import com.iteleme.backend.constant.GenderType;
import com.iteleme.backend.constant.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private String nickname;
    private String password;
    private String phone;
    private String avatar;
    private GenderType gender;
    private UserRole role;
    private Integer status;
    private String currentTokenHash;
}
