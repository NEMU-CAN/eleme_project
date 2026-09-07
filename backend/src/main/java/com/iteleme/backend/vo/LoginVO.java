package com.iteleme.backend.vo;

// ============================================================
// [阶段① 新增] 登录响应体：token + 用户信息
// ============================================================
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginVO {
    /** 登录令牌，前端后续请求需带 Authorization: Bearer <token>。 */
    private String token;
    /** 用户信息。 */
    private UserVO user;
}
