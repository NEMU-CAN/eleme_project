package com.iteleme.backend.entity;

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
    private Integer gender;
    private Integer role;
    private Integer status;
    private String currentTokenHash;
}
