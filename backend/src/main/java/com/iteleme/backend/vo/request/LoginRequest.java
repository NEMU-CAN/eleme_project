package com.iteleme.backend.vo.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String userId;
    private String password;
}
